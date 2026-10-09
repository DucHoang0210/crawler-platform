package com.dev.interceptor;

import com.dev.cache.TenantAccessCache;
import com.dev.cache.TenantAccessInfo;
import com.dev.context.TenantContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TenantInterceptor
        implements HandlerInterceptor {

    private final JdbcTemplate jdbcTemplate;

    private final TenantAccessCache tenantAccessCache;


    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        /*
         * Tránh ThreadLocal cũ còn sót lại
         * nếu thread được application server tái sử dụng.
         */
        TenantContext.clear();


        String path =
                request.getRequestURI();


        // =====================================================
        // 1. OPTIONS / CORS PREFLIGHT
        // =====================================================

        if (
                "OPTIONS".equalsIgnoreCase(
                        request.getMethod()
                )
        ) {

            return true;
        }


        // =====================================================
        // 2. API KHÔNG CẦN X-TENANT-ID
        // =====================================================

        if (
                path.startsWith(
                        "/api/v1/auth/"
                )

                        || path.equals(
                        "/api/v1/tenants/register"
                )

                        || path.equals(
                        "/api/v1/tenants/my-tenants"
                )

                        || path.equals(
                        "/error"
                )

                        || path.startsWith(
                        "/swagger-ui"
                )

                        || path.startsWith(
                        "/v3/api-docs"
                )

                        || path.startsWith(
                        "/webjars/"
                )
        ) {

            return true;
        }


        // =====================================================
        // 3. KIỂM TRA AUTHENTICATION
        // =====================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                authentication == null

                        || !authentication
                        .isAuthenticated()

                        || "anonymousUser".equals(
                        authentication.getPrincipal()
                )
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "User is not authenticated"
            );


            return false;
        }


        // =====================================================
        // 4. PRINCIPAL HIỆN TẠI LÀ USERNAME STRING
        // =====================================================

        String username =
                authentication.getName();


        if (
                username == null
                        ||
                        username.isBlank()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authenticated username is missing"
            );


            return false;
        }


        // =====================================================
        // 5. ĐỌC X-TENANT-ID
        // =====================================================

        String businessTenantId =
                request.getHeader(
                        "X-Tenant-ID"
                );


        if (
                businessTenantId == null
                        ||
                        businessTenantId.isBlank()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "MISSING_TENANT_HEADER",
                    "Header X-Tenant-ID is required"
            );


            return false;
        }


        businessTenantId =
                businessTenantId.trim();


        // =====================================================
        // 6. REDIS CACHE FIRST
        // =====================================================

        Optional<TenantAccessInfo> cached =
                tenantAccessCache.get(
                        username,
                        businessTenantId
                );


        if (
                cached.isPresent()
        ) {

            TenantAccessInfo access =
                    cached.get();


            // Cache cũng phải được validate.
            if (
                    !isValidSchemaName(
                            access.schemaName()
                    )
            ) {

                sendError(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "INVALID_TENANT_SCHEMA",
                        "Tenant schema configuration is invalid"
                );


                return false;
            }


            applyTenantContext(
                    request,
                    businessTenantId,
                    access
            );


            return true;
        }


        // =====================================================
        // 7. CACHE MISS -> QUERY POSTGRESQL
        //
        // Một query duy nhất lấy:
        // - userId
        // - schemaName
        // - đồng thời kiểm tra user thuộc tenant
        // - tenant đang ACTIVE
        // =====================================================

        List<TenantAccessInfo> results =
                jdbcTemplate.query(

                        """
                        SELECT
                            u.id AS user_id,
                            t.schema_name

                        FROM public.users u

                        INNER JOIN public.tenant_users tu
                            ON tu.user_id = u.id

                        INNER JOIN public.tenants t
                            ON t.id = tu.tenant_id

                        WHERE u.username = ?
                          AND t.tenant_id = ?
                          AND t.status = 'ACTIVE'
                        """,

                        (
                                rs,
                                rowNum
                        ) ->
                                new TenantAccessInfo(

                                        rs.getLong(
                                                "user_id"
                                        ),

                                        rs.getString(
                                                "schema_name"
                                        )
                                ),

                        username,
                        businessTenantId
                );


        // =====================================================
        // 8. KHÔNG CÓ QUYỀN TENANT / TENANT KHÔNG ACTIVE
        // =====================================================

        if (
                results.isEmpty()
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN",
                    "Access denied to this tenant or tenant inactive"
            );


            return false;
        }


        TenantAccessInfo access =
                results.get(0);


        // =====================================================
        // 9. VALIDATE SCHEMA LẤY TỪ DATABASE
        // =====================================================

        if (
                !isValidSchemaName(
                        access.schemaName()
                )
        ) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INVALID_TENANT_SCHEMA",
                    "Tenant schema configuration is invalid"
            );


            return false;
        }


        // =====================================================
        // 10. LƯU KẾT QUẢ VÀO REDIS
        // =====================================================

        tenantAccessCache.put(
                username,
                businessTenantId,
                access
        );


        // =====================================================
        // 11. SET TENANT CONTEXT
        //
        // KHÔNG SET search_path tại interceptor.
        // TenantExecutionService sẽ xử lý search_path.
        // =====================================================

        applyTenantContext(
                request,
                businessTenantId,
                access
        );


        return true;
    }


    // =========================================================
    // APPLY TENANT INFORMATION
    // =========================================================

    private void applyTenantContext(
            HttpServletRequest request,
            String businessTenantId,
            TenantAccessInfo access
    ) {

        TenantContext.setCurrentTenant(
                access.schemaName()
        );


        request.setAttribute(
                "tenantId",
                businessTenantId
        );


        request.setAttribute(
                "tenantSchema",
                access.schemaName()
        );


        request.setAttribute(
                "currentUserId",
                access.userId()
        );
    }


    // =========================================================
    // VALIDATE SCHEMA NAME
    // =========================================================

    private boolean isValidSchemaName(
            String schemaName
    ) {

        return schemaName != null

                && schemaName.matches(
                "^schema_[a-z0-9_]+$"
        );
    }


    // =========================================================
    // REQUEST HOÀN THÀNH
    // =========================================================

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {

        TenantContext.clear();
    }


    // =========================================================
    // JSON ERROR RESPONSE
    // =========================================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(
                status
        );


        response.setContentType(
                "application/json"
        );


        response.setCharacterEncoding(
                "UTF-8"
        );


        response
                .getWriter()
                .write(
                        String.format(
                                """
                                {
                                  "error": "%s",
                                  "message": "%s"
                                }
                                """,
                                code,
                                message
                        )
                );
    }
}