package com.dev.service;

import com.dev.scheduler.PriceMonitorJobHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class PriceMonitorJobExecutor
        implements PriceMonitorJobHandler {

    private final TenantExecutionService tenantExecutionService;

    private final PriceMonitorService priceMonitorService;

    private final PriceMonitorLockService priceMonitorLockService;


    @Override
    public void execute(
            String schemaName,
            Long listingId
    ) {

        // STEP 1
        // TRY TO ACQUIRE DISTRIBUTED LOCK

        String lockToken =
                priceMonitorLockService
                        .tryLock(
                                schemaName,
                                listingId
                        );


        // STEP 2
        // SOMEONE IS ALREADY MONITORING THIS LISTING

        if (lockToken == null) {

            log.info(
                    "Price monitor skipped because another execution is already running. schema={}, listingId={}",
                    schemaName,
                    listingId
            );

            return;
        }


        try {

            // STEP 3
            // ENTER CORRECT TENANT SCHEMA

            tenantExecutionService.execute(
                    schemaName,

                    () -> priceMonitorService.monitor(listingId)
            );


        } finally {

            // STEP 4
            // ALWAYS RELEASE LOCK

            priceMonitorLockService.unlock(
                    schemaName,
                    listingId,
                    lockToken
            );
        }
    }
}