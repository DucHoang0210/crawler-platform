package com.dev.notification.sender;

import com.dev.domain.WebhookConfig;
import com.dev.dto.PriceAlertEvent;
import com.dev.notification.AlertSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TelegramAlertSender
        implements AlertSender {

    private final WebClient.Builder webClientBuilder;

    @Override
    public boolean supports(String type) {

        return "TELEGRAM".equalsIgnoreCase(type);
    }

    @Override
    public void send(
            WebhookConfig config,
            PriceAlertEvent event
    ) {

        validateConfig(config);

        String message =
                String.format(
                        """
                        🚨 COMPETITOR PRICE ALERT

                        Platform: %s
                        Competitor: %s

                        Old price: %s
                        New price: %s

                        Drop: %s%%
                        """,

                        event.getPlatform(),
                        event.getCompetitorName(),
                        event.getOldPrice(),
                        event.getNewPrice(),
                        event.getDropPercent()
                );

        String telegramUrl =
                "https://api.telegram.org/bot"
                        + config.getSecretKey()
                        + "/sendMessage";

        webClientBuilder
                .build()
                .post()
                .uri(telegramUrl)
                .bodyValue(
                        Map.of(
                                "chat_id",
                                config.getDestination(),
                                "text",
                                message
                        )
                )
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void validateConfig(
            WebhookConfig config
    ) {

        if (config.getSecretKey() == null
                || config.getSecretKey().isBlank()) {

            throw new IllegalArgumentException(
                    "Telegram bot token is required"
            );
        }

        if (config.getDestination() == null
                || config.getDestination().isBlank()) {

            throw new IllegalArgumentException(
                    "Telegram chat id is required"
            );
        }
    }
}