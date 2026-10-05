package com.dev.notification;

import com.dev.domain.WebhookConfig;
import com.dev.dto.PriceAlertEvent;

public interface AlertSender {

    boolean supports(String type);

    void send(
            WebhookConfig config,
            PriceAlertEvent event
    );
}