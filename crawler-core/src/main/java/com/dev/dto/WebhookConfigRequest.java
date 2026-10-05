package com.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "WebhookConfigRequest",
        description =
                "Create a webhook integration for the current tenant"
)
public class WebhookConfigRequest {

    @NotBlank(
            message =
                    "Webhook name is required"
    )
    @Schema(
            description =
                    "Display name of the integration",
            example =
                    "Main Telegram Alert",
            requiredMode =
                    Schema.RequiredMode.REQUIRED
    )
    private String name;


    @NotBlank(
            message =
                    "Webhook type is required"
    )
    @Schema(
            description =
                    "Integration type",
            example =
                    "TELEGRAM",
            allowableValues = {
                    "TELEGRAM",
                    "ERP",
                    "ZALO"
            },
            requiredMode =
                    Schema.RequiredMode.REQUIRED
    )
    private String type;


    @Schema(
            description =
                    """
                    External endpoint URL.

                    Required when type = ERP.
                    """,
            example =
                    "https://erp.example.com/api/price-alerts"
    )
    private String targetUrl;


    @Schema(
            description =
                    """
                    Secret used by the integration.

                    TELEGRAM:
                    Telegram Bot Token.

                    ERP:
                    API key/shared secret if required.
                    """,
            example =
                    "********",
            accessMode =
                    Schema.AccessMode.WRITE_ONLY
    )
    private String secretKey;


    @Schema(
            description =
                    """
                    Destination identifier.

                    Required when type = TELEGRAM.
                    For Telegram this is the Chat ID.
                    """,
            example =
                    "123456789"
    )
    private String destination;
}