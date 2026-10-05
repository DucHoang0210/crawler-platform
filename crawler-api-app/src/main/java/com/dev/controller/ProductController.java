package com.dev.controller;

import com.dev.config.OpenApiConfig;
import com.dev.context.TenantContext;
import com.dev.dto.CreateProductRequest;
import com.dev.dto.ProductResponse;
import com.dev.service.ProductService;
import com.dev.service.TenantExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/products"
)
@RequiredArgsConstructor
@SecurityRequirement(
        name = OpenApiConfig.BEARER_AUTH
)
public class ProductController {

    private final ProductService
            productService;

    private final TenantExecutionService
            tenantExecutionService;


    @GetMapping
    public List<ProductResponse> findAll() {

        String schemaName =
                requireTenantSchema();


        return tenantExecutionService
                .execute(
                        schemaName,
                        productService::findAll
                );
    }


    @GetMapping("/{id}")
    public ProductResponse findById(
            @PathVariable Long id
    ) {

        String schemaName =
                requireTenantSchema();


        return tenantExecutionService
                .execute(
                        schemaName,
                        () ->
                                productService
                                        .findById(id)
                );
    }


    @PostMapping
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public ProductResponse create(
            @Valid
            @RequestBody
            CreateProductRequest request
    ) {

        String schemaName =
                requireTenantSchema();


        return tenantExecutionService
                .execute(
                        schemaName,
                        () ->
                                productService
                                        .create(
                                                request
                                        )
                );
    }


    private String requireTenantSchema() {

        String schemaName =
                TenantContext
                        .getCurrentTenant();


        if (
                schemaName == null
                        || schemaName.isBlank()
        ) {

            throw new IllegalStateException(
                    "Tenant schema is not available"
            );
        }


        return schemaName;
    }
}