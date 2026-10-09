package com.dev.job;

import com.dev.repository.TenantRepository;

import com.dev.service.WebhookDispatchExecutor;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertOutboxScheduler {

    private final TenantRepository
            tenantRepository;

    private final WebhookDispatchExecutor
            webhookDispatchExecutor;


    @Scheduled(
            initialDelayString =
                    "${crawler.outbox.initial-delay-ms:10000}",

            fixedDelayString =
                    "${crawler.outbox.dispatch-delay-ms:3000}"
    )
    public void dispatch() {

        tenantRepository
                .findAll()

                .forEach(
                        tenant -> {

                            if (
                                    tenant.getStatus() == null
                                            ||
                                            !"ACTIVE".equalsIgnoreCase(
                                                    tenant.getStatus().toString()
                                            )
                            ) {
                                return;
                            }


                            try {

                                webhookDispatchExecutor
                                        .execute(
                                                tenant
                                                        .getSchemaName()
                                        );

                            } catch (
                                    Exception e
                            ) {

                                log.error(
                                        "Outbox dispatch failed for tenant {}",
                                        tenant.getTenantId(),
                                        e
                                );
                            }
                        }
                );
    }
}