package com.dev.notification;

import com.dev.dto.PriceAlertEvent;

public interface AlertPublisher {

    void publish(PriceAlertEvent event);
}