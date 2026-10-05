package com.dev.service;

import com.dev.domain.AlertOutbox;
import com.dev.dto.PriceAlertEvent;
import com.dev.notification.AlertPublisher;
import com.dev.repository.AlertOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxAlertPublisher
        implements AlertPublisher {

    private final AlertOutboxRepository
            outboxRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void publish(
            PriceAlertEvent event
    ) {

        try {

            String payload =
                    objectMapper.writeValueAsString(
                            event
                    );

            AlertOutbox outbox =
                    AlertOutbox.builder()

                            .eventId(
                                    UUID.randomUUID()
                            )

                            .eventType(
                                    "COMPETITOR_PRICE_DROP"
                            )

                            .payload(
                                    payload
                            )

                            .status(
                                    "PENDING"
                            )

                            .retryCount(
                                    0
                            )

                            .createdAt(
                                    LocalDateTime.now()
                            )

                            .build();

            outboxRepository.save(
                    outbox
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Cannot serialize price alert",
                    e
            );
        }
    }
}