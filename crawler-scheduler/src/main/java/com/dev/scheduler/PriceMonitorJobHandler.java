package com.dev.scheduler;

public interface PriceMonitorJobHandler {

    void execute(
            String schemaName,
            Long listingId
    );
}