package com.dev.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH =
            "bearerAuth";

    public static final String TENANT_HEADER =
            "TenantIdHeader";


    @Bean
    public OpenAPI crawlerPlatformOpenApi() {

        // =====================================================
        // BEARER AUTH
        // =====================================================

        SecurityScheme bearerScheme =
                new SecurityScheme()

                        .type(
                                SecurityScheme.Type.HTTP
                        )

                        .scheme(
                                "bearer"
                        )

                        .bearerFormat(
                                "Opaque Session Token"
                        )

                        .description(
                                """
                                Login bằng:

                                POST /api/v1/auth/login

                                Sau đó sử dụng token:

                                Authorization: Bearer <token>
                                """
                        );


        // =====================================================
        // X-TENANT-ID
        // =====================================================

        Parameter tenantHeader =
                new Parameter()

                        .name(
                                "X-Tenant-ID"
                        )

                        .in(
                                "header"
                        )

                        .required(
                                true
                        )

                        .description(
                                """
                                Tenant identifier của tenant
                                hiện tại.

                                Ví dụ:

                                tenant_shop_bo_dzai

                                Header này được TenantInterceptor
                                sử dụng để xác định tenant/schema.
                                """
                        )

                        .example(
                                "tenant_shop_bo_dzai"
                        );


        // =====================================================
        // COMPONENTS
        // =====================================================

        Components components =
                new Components()

                        .addSecuritySchemes(
                                BEARER_AUTH,
                                bearerScheme
                        )

                        .addParameters(
                                TENANT_HEADER,
                                tenantHeader
                        );


        // =====================================================
        // OPEN API
        // =====================================================

        return new OpenAPI()

                .info(
                        new Info()

                                .title(
                                        "Crawler Platform API"
                                )

                                .version(
                                        "1.0.0"
                                )

                                .description(
                                        """
                                        Multi-tenant price intelligence
                                        and competitor monitoring platform.

                                        Authentication:
                                        Bearer session token.

                                        Tenant-scoped API:
                                        X-Tenant-ID header.
                                        """
                                )
                )

                .components(
                        components
                );
    }
}