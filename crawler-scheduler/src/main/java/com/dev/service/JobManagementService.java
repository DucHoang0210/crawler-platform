package com.dev.job;

import com.dev.domain.JobConfig;
import com.dev.domain.JobStatus;
import com.dev.repository.JobConfigRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobManagementService {

    private final Scheduler scheduler;
    private final JobConfigRepository jobConfigRepository;

    public void scheduleCrawlerJob(String tenantId, String jobName, String cronExpression, String url, String titleSelector, String priceSelector) {
        try {
            if (!CronExpression.isValidExpression(cronExpression)) {
                throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
            }

            JobConfig jobConfig = JobConfig.builder()
                    .jobName(jobName)
                    .targetUrl(url)
                    .cronExpression(cronExpression)
                    .cssSelector(titleSelector)
                    .xpathExpression(priceSelector)
                    .status(JobStatus.SUCCESS)
                    .updatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            jobConfig = jobConfigRepository.save(jobConfig);

            JobKey jobKey = new JobKey(jobName, tenantId);
            JobDetail jobDetail = JobBuilder.newJob(DynamicCrawlerJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("tenantId", tenantId)
                    .usingJobData("jobConfigId", jobConfig.getId())
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

    public void updateCronExpression(String tenantId, String jobName, String newCronExpression) throws SchedulerException {
        if (!CronExpression.isValidExpression(newCronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + newCronExpression);
        }

        TriggerKey triggerKey = new TriggerKey(jobName + "_trigger", tenantId);
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            throw new IllegalArgumentException("Trigger not found for job: " + jobName);
        }

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(new JobKey(jobName, tenantId))
                .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpression))
                .build();

        scheduler.rescheduleJob(triggerKey, newTrigger);
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