package com.dev.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class TenantOpenApiCustomizer {


    @Bean
    public OpenApiCustomizer tenantHeaderCustomizer() {

        return openApi -> {

            if (openApi.getPaths() == null) {
                return;
            }


            // =================================================
            // DUYỆT TOÀN BỘ API PATH
            // =================================================

            openApi.getPaths().forEach(
                    (path, pathItem) -> {

                        // -------------------------------------
                        // API không thuộc tenant
                        // -------------------------------------

                        if (!requiresTenant(path)) {
                            return;
                        }


                        // -------------------------------------
                        // Duyệt GET / POST / PUT / DELETE...
                        // -------------------------------------

                        Map<PathItem.HttpMethod, Operation>
                                operations =
                                pathItem.readOperationsMap();


                        for (
                                Operation operation :
                                operations.values()
                        ) {

                            addTenantHeader(
                                    operation
                            );
                        }
                    }
            );
        };
    }


    // =========================================================
    // TENANT RULE
    // =========================================================

    private boolean requiresTenant(
            String path
    ) {

        if (path == null
                || path.isBlank()) {

            return false;
        }


        // =====================================================
        // AUTH APIs
        // =====================================================
        // login / register / logout
        // không cần tenant header
        // =====================================================

        if (path.startsWith(
                "/api/v1/auth/"
        )) {

            return false;
        }


        // =====================================================
        // TENANT BOOTSTRAP APIs
        // =====================================================
        // User chưa chọn tenant ở thời điểm này
        // =====================================================

        if (path.equals(
                "/api/v1/tenants/register"
        )) {

            return false;
        }


        if (path.equals(
                "/api/v1/tenants/my-tenants"
        )) {

            return false;
        }


        // =====================================================
        // TẤT CẢ BUSINESS API CÒN LẠI
        // =====================================================

        return path.startsWith(
                "/api/v1/"
        );
    }


    // =========================================================
    // ADD X-TENANT-ID
    // =========================================================

    private void addTenantHeader(
            Operation operation
    ) {

        // Nếu endpoint đã có X-Tenant-ID
        // thì không thêm lần thứ hai.

        if (hasTenantHeader(operation)) {
            return;
        }


        Parameter tenantParameter =
                new Parameter()

                        .$ref(
                                "#/components/parameters/"
                                        + OpenApiConfig.TENANT_HEADER
                        );


        operation.addParametersItem(
                tenantParameter
        );
    }


    // =========================================================
    // DUPLICATE CHECK
    // =========================================================

    private boolean hasTenantHeader(
            Operation operation
    ) {

        List<Parameter> parameters =
                operation.getParameters();


        if (parameters == null
                || parameters.isEmpty()) {

            return false;
        }


        return parameters.stream()
                .anyMatch(
                        parameter -> {

                            // ---------------------------------
                            // Trường hợp @RequestHeader
                            // ---------------------------------

                            if (
                                    "X-Tenant-ID"
                                            .equalsIgnoreCase(
                                                    parameter.getName()
                                            )
                            ) {

                                return true;
                            }


                            // ---------------------------------
                            // Trường hợp đã dùng $ref
                            // ---------------------------------

                            String ref =
                                    parameter.get$ref();


                            return ref != null
                                    && ref.endsWith(
                                    "/"
                                            + OpenApiConfig
                                            .TENANT_HEADER
                            );
                        }
                );
    }
}