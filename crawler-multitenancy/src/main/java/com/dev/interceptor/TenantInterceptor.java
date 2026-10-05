package com.dev.interceptor;

import com.dev.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        /*
         * Tránh ThreadLocal cũ còn sót lại nếu thread
         * được application server tái sử dụng.
         */
        TenantContext.clear();

        String path = request.getRequestURI();

        // =====================================================
        // 1. OPTIONS / CORS preflight
        // =====================================================
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // =====================================================
        // 2. Các API không cần X-Tenant-ID
        // =====================================================
        if (
                path.startsWith("/api/v1/auth/")
                        || path.equals("/api/v1/tenants/register")
                        || path.equals("/api/v1/tenants/my-tenants")

                        // Swagger / OpenAPI
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
        // 3. Kiểm tra Authentication
        // =====================================================
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
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
        // 4. Principal hiện tại là username String
        // =====================================================
        String username =
                authentication.getName();

        if (username == null || username.isBlank()) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authenticated username is missing"
            );

            return false;
        }

        // =====================================================
        // 5. Tìm userId trong public.users
        // =====================================================
        Long userId;

        try {

            userId = jdbcTemplate.queryForObject(
                    """
                    SELECT id
                    FROM public.users
                    WHERE username = ?
                    """,
                    Long.class,
                    username
            );

        } catch (EmptyResultDataAccessException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authenticated user not found"
            );

            return false;
        }

        if (userId == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authenticated user not found"
            );

            return false;
        }

        // =====================================================
        // 6. Đọc X-Tenant-ID
        // VD: tenant_hoang_duc
        // =====================================================
        String businessTenantId =
                request.getHeader("X-Tenant-ID");

        if (
                businessTenantId == null
                        || businessTenantId.isBlank()
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
        // 7. Kiểm tra:
        //
        // - Tenant có tồn tại
        // - Tenant ACTIVE
        // - User có thuộc tenant
        // =====================================================
        String sql = """
                SELECT t.schema_name
                FROM public.tenants t
                INNER JOIN public.tenant_users tu
                    ON tu.tenant_id = t.id
                WHERE t.tenant_id = ?
                  AND tu.user_id = ?
                  AND t.status = 'ACTIVE'
                """;

        List<String> schemas =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                rs.getString("schema_name"),
                        businessTenantId,
                        userId
                );

        // =====================================================
        // 8. Không có quyền tenant
        // =====================================================
        if (schemas.isEmpty()) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN",
                    "Access denied to this tenant or tenant inactive"
            );

            return false;
        }

        String targetSchema =
                schemas.get(0);

        // =====================================================
        // 9. Validate schema lấy từ DB
        // =====================================================
        if (
                targetSchema == null
                        || !targetSchema.matches(
                        "^schema_[a-z0-9_]+$"
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
        // 10. Chỉ SET TenantContext
        //
        // KHÔNG SET search_path tại đây.
        // =====================================================
        TenantContext.setCurrentTenant(
                targetSchema
        );

        /*
         * Optional:
         * lưu thêm thông tin vào request nếu Controller cần.
         */
        request.setAttribute(
                "tenantId",
                businessTenantId
        );

        request.setAttribute(
                "tenantSchema",
                targetSchema
        );

        request.setAttribute(
                "currentUserId",
                userId
        );

        return true;
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

        response.setStatus(status);
        response.setContentType(
                "application/json"
        );
        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
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