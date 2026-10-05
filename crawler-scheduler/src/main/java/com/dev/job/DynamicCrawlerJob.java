package com.dev.job;

import com.dev.context.TenantContext;
import com.dev.domain.JobConfig;
import com.dev.domain.JobExecutionLog;
import com.dev.domain.JobStatus;
import com.dev.entity.ScraperResponse;
import com.dev.service.JsoupScraperService;
import com.dev.repository.JobConfigRepository;
import com.dev.repository.JobExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicCrawlerJob extends QuartzJobBean {

    private final JsoupScraperService jsoupScraperService;
    private final JobConfigRepository jobConfigRepository;
    private final JobExecutionLogRepository jobExecutionLogRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String tenantId = dataMap.getString("tenantId");
        Long jobConfigId = dataMap.getLong("jobConfigId");

        TenantContext.setCurrentTenant(tenantId);
        JobExecutionLog executionLog = JobExecutionLog.builder()
                .tenantId(tenantId)
                .jobId(jobConfigId)
                .startTime(java.time.LocalDateTime.now())
                .status(JobStatus.FAILED)
                .recordsFetched(0)
                .build();

        try {
            JobConfig jobConfig = jobConfigRepository.findByIdAndTenantId(jobConfigId, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Job config not found: " + jobConfigId));

            executionLog = jobExecutionLogRepository.save(executionLog);

            log.info("Executing Job [{}] for Tenant: [{}] - URL: {}", jobConfig.getJobName(), tenantId, jobConfig.getTargetUrl());
            ScraperResponse result = jsoupScraperService.scrape(
                    jobConfig.getTargetUrl(),
                    jobConfig.getCssSelector(),
                    jobConfig.getXpathExpression()
            );
            log.info("Scraped Successfully: Title = {}, Price = {}", result.getTitle(), result.getCurrentPrice());

            executionLog.setEndTime(java.time.LocalDateTime.now());
            executionLog.setStatus(JobStatus.SUCCESS);
            executionLog.setRecordsFetched(1);
            executionLog.setErrorMessage(null);
            jobExecutionLogRepository.save(executionLog);
        } catch (Exception e) {
            executionLog.setEndTime(java.time.LocalDateTime.now());
            executionLog.setStatus(JobStatus.FAILED);
            executionLog.setRecordsFetched(0);
            executionLog.setErrorMessage(e.getMessage());
            jobExecutionLogRepository.save(executionLog);
            log.error("Error executing crawler job for configId: {}", jobConfigId, e);
        } finally {
            TenantContext.clear();
        }
    }
}