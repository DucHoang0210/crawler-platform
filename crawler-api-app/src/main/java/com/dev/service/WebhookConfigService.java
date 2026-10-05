package com.dev.service;

import com.dev.domain.WebhookConfig;
import com.dev.dto.WebhookConfigRequest;
import com.dev.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WebhookConfigService {

    private final WebhookConfigRepository repository;

    public WebhookConfig create(
            WebhookConfigRequest request
    ) {

        if (request.getType() == null
                || request.getType().isBlank()) {
            throw new IllegalArgumentException(
                    "Webhook type is required"
            );
        }

        WebhookConfig config =
                WebhookConfig.builder()

                        .name(
                                request.getName()
                        )

                        .type(
                                request.getType()
                                        .trim()
                                        .toUpperCase(Locale.ROOT)
                        )

                        .targetUrl(
                                request.getTargetUrl()
                        )

                        .secretKey(
                                request.getSecretKey()
                        )

                        .destination(
                                request.getDestination()
                        )

                        .isActive(true)

                        .build();

        return repository.save(config);
    }

    public List<WebhookConfig> getAll() {
        return repository.findAll();
    }
}