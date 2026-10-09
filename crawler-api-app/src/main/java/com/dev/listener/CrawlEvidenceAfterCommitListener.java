package com.dev.listener;


import com.dev.event.PriceSnapshotEvidenceEvent;

import com.dev.service.CrawlEvidenceService;
import com.dev.service.TenantExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlEvidenceAfterCommitListener {


    private final TenantExecutionService
            tenantExecutionService;


    private final CrawlEvidenceService
            crawlEvidenceService;


    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            PriceSnapshotEvidenceEvent event
    ) {

        try {

            tenantExecutionService.execute(

                    event.schemaName(),

                    () ->
                            crawlEvidenceService
                                    .saveRawEvidence(
                                            event.snapshotId(),
                                            event.listingId(),
                                            event.rawPayload(),
                                            event.contentType()
                                    )
            );


        } catch (Exception e) {

            /*
             * Không throw ngược ra Price Monitor.
             *
             * Snapshot đã commit thành công.
             * Evidence là dữ liệu bổ sung.
             */

            log.warn(
                    "Crawl evidence storage failed after snapshot commit. schema={}, listingId={}, snapshotId={}",
                    event.schemaName(),
                    event.listingId(),
                    event.snapshotId(),
                    e
            );
        }
    }
}