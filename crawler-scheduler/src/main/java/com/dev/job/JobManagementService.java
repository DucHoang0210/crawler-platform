package com.dev.job;

import com.dev.job.DynamicCrawlerJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobManagementService {

    private final Scheduler scheduler;

    public void scheduleCrawlerJob(String tenantId, String jobName, String cronExpression, String url, String titleSelector, String priceSelector) {
        try {
            JobKey jobKey = new JobKey(jobName, tenantId);
            JobDetail jobDetail = JobBuilder.newJob(DynamicCrawlerJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("tenantId", tenantId)
                    .usingJobData("url", url)
                    .usingJobData("titleSelector", titleSelector)
                    .usingJobData("priceSelector", priceSelector)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_trigger", tenantId)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule Quartz Job", e);
        }
    }

    public void pauseJob(String tenantId, String jobName) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobName, tenantId));
    }

    public void resumeJob(String tenantId, String jobName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName, tenantId));
    }

    public void deleteJob(String tenantId, String jobName) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobName, tenantId));
    }
}