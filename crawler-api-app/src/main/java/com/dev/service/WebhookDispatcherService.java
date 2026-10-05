package com.dev.service;

import com.dev.domain.AlertOutbox;
import com.dev.dto.PriceAlertEvent;
import com.dev.repository.AlertOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookDispatcherService {

    private final AlertOutboxRepository
            outboxRepository;

    private final WebhookService
            webhookService;

    private final ObjectMapper
            objectMapper;

    // =========================================================
    // GET PENDING OUTBOX
    // =========================================================
    public List<AlertOutbox> getPending() {

        return outboxRepository.findPending(
                LocalDateTime.now()
        );
    }

    // =========================================================
    // GET OUTBOX BY ID
    // =========================================================
    public AlertOutbox getById(
            Long id
    ) {

        return outboxRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Outbox event not found: "
                                        + id
                        )
                );
    }

    // =========================================================
    // DISPATCH
    // =========================================================
    public void dispatch(
            AlertOutbox outbox
    ) {

        try {

            PriceAlertEvent event =
                    objectMapper.readValue(
                            outbox.getPayload(),
                            PriceAlertEvent.class
                    );

            // Telegram / ERP / Zalo...
            webhookService.send(
                    event
            );

            // Thành công
            outbox.setStatus(
                    "SENT"
            );

            outbox.setSentAt(
                    LocalDateTime.now()
            );

            outbox.setLastError(
                    null
            );

            outbox.setNextRetryAt(
                    null
            );

            outboxRepository.save(
                    outbox
            );

        } catch (Exception e) {

            int retries =
                    outbox.getRetryCount() == null
                            ? 1
                            : outbox.getRetryCount() + 1;

            outbox.setRetryCount(
                    retries
            );

            outbox.setLastError(
                    e.getMessage()
            );

            if (retries >= 5) {

                outbox.setStatus(
                        "FAILED"
                );

                outbox.setNextRetryAt(
                        null
                );

            } else {

                outbox.setStatus(
                        "PENDING"
                );

                outbox.setNextRetryAt(
                        LocalDateTime.now()
                                .plusMinutes(
                                        retries * 2L
                                )
                );
            }

            outboxRepository.save(
                    outbox
            );
        }
    }
}