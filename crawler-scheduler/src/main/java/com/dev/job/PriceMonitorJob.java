package com.dev.job;

import com.dev.scheduler.PriceMonitorJobHandler;
import lombok.RequiredArgsConstructor;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;


@DisallowConcurrentExecution
@RequiredArgsConstructor
public class PriceMonitorJob
        implements Job {

    private final PriceMonitorJobHandler
            handler;


    @Override
    public void execute(
            JobExecutionContext context
    ) {

        String schemaName =
                context
                        .getMergedJobDataMap()
                        .getString(
                                "schemaName"
                        );


        Long listingId =
                context
                        .getMergedJobDataMap()
                        .getLong(
                                "listingId"
                        );


        handler.execute(
                schemaName,
                listingId
        );
    }
}