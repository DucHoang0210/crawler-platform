package com.dev.controller;

import com.dev.config.OpenApiConfig;
import com.dev.context.TenantContext;
import com.dev.domain.WebhookConfig;
import com.dev.dto.WebhookConfigRequest;
import com.dev.service.TenantExecutionService;
import com.dev.service.WebhookConfigService;
import com.dev.service.WebhookDispatchExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhook Management", description = "APIs for managing webhook configurations and dispatching webhooks")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class WebhookController {

    private final TenantExecutionService
            tenantExecutionService;

    private final WebhookConfigService
            webhookConfigService;

    private final WebhookDispatchExecutor
            dispatchExecutor;

    @Operation(
            summary =
                    "Create webhook integration",

            description =
                    """
                    Create a webhook integration
                    for the current tenant.

                    Supported types:
                    - TELEGRAM
                    - ERP
                    - ZALO
                    """,

            parameters = {
                    @Parameter(
                            name =
                                    "X-Tenant-ID",
                            description =
                                    "Current tenant identifier",
                            required = true,
                            in =
                                    ParameterIn.HEADER,
                            example =
                                    "tenant_shop_bo_dzai"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<?> create(

            @Valid
            @RequestBody
            WebhookConfigRequest request
    ) {

        return ResponseEntity.ok(
                webhookConfigService.create(
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAll() {

        String schemaName =
                TenantContext.getCurrentTenant();

        return ResponseEntity.ok(
                tenantExecutionService.execute(
                        schemaName,
                        webhookConfigService::getAll
                )
        );
    }

    @Operation(
            summary =
                    "Dispatch pending notifications",

            description =
                    """
                    Read PENDING events from alert_outbox
                    and send them to all active integrations.
                    """,

            parameters = {
                    @Parameter(
                            name =
                                    "X-Tenant-ID",
                            description =
                                    "Current tenant identifier",
                            required = true,
                            in =
                                    ParameterIn.HEADER,
                            example =
                                    "tenant_shop_bo_dzai"
                    )
            }
    )
    @PostMapping("/dispatch")
    public ResponseEntity<?> dispatch() {

        String schemaName =
                TenantContext.getCurrentTenant();

        dispatchExecutor.execute(
                schemaName
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Outbox dispatch completed"
                )
        );
    }
}