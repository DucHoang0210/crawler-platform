package com.dev.notification.sender;

import com.dev.domain.WebhookConfig;
import com.dev.dto.PriceAlertEvent;
import com.dev.notification.AlertSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ErpWebhookSender
        implements AlertSender {

    private final WebClient.Builder webClientBuilder;

    @Override
    public boolean supports(String type) {

        return "ERP".equalsIgnoreCase(type);
    }

    @Override
    public void send(
            WebhookConfig config,
            PriceAlertEvent event
    ) {

        validateConfig(config);

        WebClient.RequestBodySpec request =
                webClientBuilder
                        .build()
                        .post()
                        .uri(
                                config.getTargetUrl()
                        );

        // API key là optional.
        // Nếu ERP yêu cầu API key thì gửi header.
        if (config.getSecretKey() != null
                && !config.getSecretKey().isBlank()) {

            request.header(
                    "X-API-Key",
                    config.getSecretKey()
            );
        }

        request
                .bodyValue(event)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void validateConfig(
            WebhookConfig config
    ) {

        if (config.getTargetUrl() == null
                || config.getTargetUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "ERP webhook URL is required"
            );
        }
    }
}