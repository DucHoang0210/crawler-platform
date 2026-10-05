package com.dev.controller;

import com.dev.domain.DemoProduct;
import com.dev.dto.CreateDemoProductRequest;
import com.dev.dto.UpdateDemoPriceRequest;
import com.dev.service.DemoProductService;
import com.dev.service.TenantExecutionService;
import com.dev.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/demo-products"
)
@RequiredArgsConstructor
public class DemoProductController {

    private final DemoProductService
            demoProductService;

    private final TenantExecutionService
            tenantExecutionService;


    @PostMapping
    public DemoProduct create(
            @RequestBody
            CreateDemoProductRequest request
    ) {

        String schema =
                TenantContext
                        .getCurrentTenant();

        return tenantExecutionService.execute(
                schema,
                () ->
                        demoProductService.create(
                                request
                        )
        );
    }


    @GetMapping
    public List<DemoProduct> findAll() {

        String schema =
                TenantContext
                        .getCurrentTenant();

        return tenantExecutionService.execute(
                schema,
                demoProductService::findAll
        );
    }


    @PatchMapping("/{id}/price")
    public DemoProduct updatePrice(
            @PathVariable Long id,

            @RequestBody
            UpdateDemoPriceRequest request
    ) {

        String schema =
                TenantContext
                        .getCurrentTenant();

        return tenantExecutionService.execute(
                schema,
                () ->
                        demoProductService
                                .updatePrice(
                                        id,
                                        request
                                )
        );
    }
}