package com.dev.controller;

import com.dev.context.TenantContext;
import com.dev.dto.CompetitorResponse;
import com.dev.dto.CreateCompetitorRequest;
import com.dev.service.CompetitorService;
import com.dev.service.TenantExecutionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/products/{productId}/competitors"
)
@RequiredArgsConstructor
public class CompetitorController {

    private final CompetitorService
            competitorService;

    private final TenantExecutionService
            tenantExecutionService;


    @PostMapping
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public CompetitorResponse create(
            @PathVariable
            Long productId,

            @Valid
            @RequestBody
            CreateCompetitorRequest request
    ) {

        String schemaName =
                TenantContext
                        .getCurrentTenant();


        return tenantExecutionService
                .execute(
                        schemaName,

                        () ->
                                competitorService
                                        .create(
                                                productId,
                                                request
                                        )
                );
    }


    @GetMapping
    public List<CompetitorResponse>
    findAll(
            @PathVariable
            Long productId
    ) {

        String schemaName =
                TenantContext
                        .getCurrentTenant();


        return tenantExecutionService
                .execute(
                        schemaName,

                        () ->
                                competitorService
                                        .findByProduct(
                                                productId
                                        )
                );
    }
}