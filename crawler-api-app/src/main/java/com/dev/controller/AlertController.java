package com.dev.controller;

import com.dev.context.TenantContext;
import com.dev.dto.PriceAlertResponse;

import com.dev.service.AlertService;
import com.dev.service.TenantExecutionService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/alerts"
)
@RequiredArgsConstructor
public class AlertController {

    private final AlertService
            alertService;

    private final TenantExecutionService
            tenantExecutionService;


    @GetMapping
    public List<PriceAlertResponse>
    findAll() {

        String schema =
                TenantContext
                        .getCurrentTenant();


        return tenantExecutionService
                .execute(
                        schema,
                        alertService::findAll
                );
    }
}