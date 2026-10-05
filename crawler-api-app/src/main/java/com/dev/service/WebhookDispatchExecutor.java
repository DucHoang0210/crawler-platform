package com.dev.service;

import com.dev.domain.AlertOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookDispatchExecutor {

    private final TenantExecutionService
            tenantExecutionService;

    private final WebhookDispatcherService
            dispatcherService;

    public void execute(
            String schemaName
    ) {

        // =====================================================
        // 1. Lấy ID của các outbox đang PENDING
        // =====================================================

        List<Long> pendingIds =
                tenantExecutionService.execute(
                        schemaName,
                        () ->
                                dispatcherService
                                        .getPending()
                                        .stream()
                                        .map(
                                                AlertOutbox::getId
                                        )
                                        .toList()
                );

        // =====================================================
        // 2. Dispatch từng event
        // =====================================================

        for (Long id : pendingIds) {

            tenantExecutionService.execute(
                    schemaName,
                    () -> {

                        AlertOutbox outbox =
                                dispatcherService
                                        .getById(id);

                        dispatcherService.dispatch(
                                outbox
                        );
                    }
            );
        }
    }
}