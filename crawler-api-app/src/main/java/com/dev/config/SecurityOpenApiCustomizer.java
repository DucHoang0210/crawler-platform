package com.dev.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class SecurityOpenApiCustomizer {

    @Bean
    public OpenApiCustomizer bearerSecurityCustomizer() {

        return openApi -> {

            if (openApi.getPaths() == null) {
                return;
            }


            // =============================================
            // Duyệt toàn bộ OpenAPI paths
            // =============================================

            openApi.getPaths()
                    .forEach(
                            (path, pathItem) -> {

                                if (
                                        !requiresAuthentication(
                                                path
                                        )
                                ) {
                                    return;
                                }


                                Map<
                                        PathItem.HttpMethod,
                                        Operation
                                        > operations =
                                        pathItem
                                                .readOperationsMap();


                                for (
                                        Operation operation :
                                        operations.values()
                                ) {

                                    addBearerSecurity(
                                            operation
                                    );
                                }
                            }
                    );
        };
    }


    // =====================================================
    // ENDPOINT NÀO CẦN LOGIN?
    // =====================================================

    private boolean requiresAuthentication(
            String path
    ) {

        if (
                path == null
                        || path.isBlank()
        ) {
            return false;
        }


        if (
                !path.startsWith(
                        "/api/v1/"
                )
        ) {
            return false;
        }


        // ---------------------------------------------
        // PUBLIC AUTH APIs
        // ---------------------------------------------

        if (
                path.equals(
                        "/api/v1/auth/login"
                )
        ) {
            return false;
        }


        if (
                path.equals(
                        "/api/v1/auth/register"
                )
        ) {
            return false;
        }


        // ---------------------------------------------
        // TẤT CẢ API KHÁC CẦN BEARER TOKEN
        // ---------------------------------------------

        return true;
    }


    // =====================================================
    // ADD bearerAuth
    // =====================================================

    private void addBearerSecurity(
            Operation operation
    ) {

        if (
                hasBearerSecurity(
                        operation
                )
        ) {
            return;
        }


        SecurityRequirement requirement =
                new SecurityRequirement()
                        .addList(
                                OpenApiConfig
                                        .BEARER_AUTH
                        );


        operation.addSecurityItem(
                requirement
        );
    }


    // =====================================================
    // TRÁNH ADD bearerAuth 2 LẦN
    // =====================================================

    private boolean hasBearerSecurity(
            Operation operation
    ) {

        List<SecurityRequirement> security =
                operation.getSecurity();


        if (
                security == null
                        || security.isEmpty()
        ) {
            return false;
        }


        return security.stream()
                .anyMatch(
                        requirement ->
                                requirement.containsKey(
                                        OpenApiConfig
                                                .BEARER_AUTH
                                )
                );
    }
}