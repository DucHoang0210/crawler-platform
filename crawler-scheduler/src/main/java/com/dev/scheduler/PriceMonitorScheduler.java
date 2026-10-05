package com.dev.scheduler;

import com.dev.job.PriceMonitorJob;
import lombok.RequiredArgsConstructor;

import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PriceMonitorScheduler {

    private final Scheduler scheduler;


    // =========================================================
    // RUN ONCE AT SPECIFIC TIME
    // =========================================================

    public void scheduleOnce(
            String tenantId,
            String schemaName,
            Long listingId,
            ZonedDateTime runAt
    ) {

        try {

            /*
             * Một listing chỉ có một planned schedule.
             *
             * Nếu đã có schedule cũ:
             * replace nó.
             */
            deleteExistingSchedule(
                    tenantId,
                    listingId
            );


            JobKey jobKey =
                    buildJobKey(
                            tenantId,
                            listingId
                    );


            JobDetail jobDetail =
                    createJobDetail(
                            jobKey,
                            tenantId,
                            schemaName,
                            listingId
                    );


            Trigger trigger =
                    TriggerBuilder
                            .newTrigger()

                            .withIdentity(
                                    buildTriggerKey(
                                            tenantId,
                                            listingId
                                    )
                            )

                            .forJob(
                                    jobDetail
                            )

                            .startAt(
                                    Date.from(
                                            runAt.toInstant()
                                    )
                            )

                            .withSchedule(
                                    SimpleScheduleBuilder
                                            .simpleSchedule()

                                            /*
                                             * Một lần duy nhất.
                                             */
                                            .withRepeatCount(0)

                                            .withMisfireHandlingInstructionFireNow()
                            )

                            .build();


            scheduler.scheduleJob(
                    jobDetail,
                    trigger
            );

        } catch (SchedulerException e) {

            throw new IllegalStateException(
                    "Failed to schedule one-time price monitor",
                    e
            );
        }
    }

    // =========================================================
    // DAILY RECURRING
    // =========================================================

    public void scheduleDaily(
            String tenantId,
            String schemaName,
            Long listingId,

            ZonedDateTime startAt,
            ZonedDateTime endAt,

            LocalTime timeOfDay,

            ZoneId zoneId
    ) {

        if (
                timeOfDay == null
        ) {

            throw new IllegalArgumentException(
                    "timeOfDay is required for DAILY schedule"
            );
        }


        String cronExpression =
                buildDailyCron(
                        timeOfDay
                );


        scheduleCron(
                tenantId,
                schemaName,
                listingId,

                startAt,
                endAt,

                cronExpression,
                zoneId
        );
    }


    // =========================================================
    // WEEKLY RECURRING
    // =========================================================

    public void scheduleWeekly(
            String tenantId,
            String schemaName,
            Long listingId,

            ZonedDateTime startAt,
            ZonedDateTime endAt,

            Set<DayOfWeek> daysOfWeek,

            LocalTime timeOfDay,

            ZoneId zoneId
    ) {

        if (
                daysOfWeek == null
                        ||
                        daysOfWeek.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "daysOfWeek is required for WEEKLY schedule"
            );
        }


        String cronExpression =
                buildWeeklyCron(
                        daysOfWeek,
                        timeOfDay
                );


        scheduleCron(
                tenantId,
                schemaName,
                listingId,

                startAt,
                endAt,

                cronExpression,
                zoneId
        );
    }


    // =========================================================
    // MONTHLY RECURRING
    // =========================================================

    public void scheduleMonthly(
            String tenantId,
            String schemaName,
            Long listingId,

            ZonedDateTime startAt,
            ZonedDateTime endAt,

            int dayOfMonth,

            LocalTime timeOfDay,

            ZoneId zoneId
    ) {

        if (
                dayOfMonth < 1
                        ||
                        dayOfMonth > 31
        ) {

            throw new IllegalArgumentException(
                    "dayOfMonth must be between 1 and 31"
            );
        }


        String cronExpression =
                buildMonthlyCron(
                        dayOfMonth,
                        timeOfDay
                );


        scheduleCron(
                tenantId,
                schemaName,
                listingId,

                startAt,
                endAt,

                cronExpression,
                zoneId
        );
    }


    // =========================================================
    // COMMON CRON SCHEDULER
    // =========================================================

    private void scheduleCron(
            String tenantId,
            String schemaName,
            Long listingId,

            ZonedDateTime startAt,
            ZonedDateTime endAt,

            String cronExpression,

            ZoneId zoneId
    ) {

        try {

            deleteExistingSchedule(
                    tenantId,
                    listingId
            );


            JobKey jobKey =
                    buildJobKey(
                            tenantId,
                            listingId
                    );


            JobDetail jobDetail =
                    createJobDetail(
                            jobKey,
                            tenantId,
                            schemaName,
                            listingId
                    );


            CronScheduleBuilder cronSchedule =
                    CronScheduleBuilder

                            .cronSchedule(
                                    cronExpression
                            )

                            .inTimeZone(
                                    TimeZone.getTimeZone(
                                            zoneId
                                    )
                            )

                            /*
                             * Nếu server tắt đúng thời điểm
                             * cần chạy thì khi bật lại
                             * không chạy bù hàng loạt.
                             */
                            .withMisfireHandlingInstructionDoNothing();


            Trigger trigger =
                    TriggerBuilder
                            .newTrigger()

                            .withIdentity(
                                    buildTriggerKey(
                                            tenantId,
                                            listingId
                                    )
                            )

                            .forJob(
                                    jobDetail
                            )

                            .startAt(
                                    Date.from(
                                            startAt.toInstant()
                                    )
                            )

                            .endAt(
                                    Date.from(
                                            endAt.toInstant()
                                    )
                            )

                            .withSchedule(
                                    cronSchedule
                            )

                            .build();


            scheduler.scheduleJob(
                    jobDetail,
                    trigger
            );

        } catch (SchedulerException e) {

            throw new IllegalStateException(
                    "Failed to schedule recurring price monitor",
                    e
            );
        }
    }


    // =========================================================
    // CANCEL
    // =========================================================

    public boolean cancel(
            String tenantId,
            Long listingId
    ) {

        try {

            return scheduler.deleteJob(
                    buildJobKey(
                            tenantId,
                            listingId
                    )
            );

        } catch (SchedulerException e) {

            throw new IllegalStateException(
                    "Failed to cancel price monitor schedule",
                    e
            );
        }
    }


    // =========================================================
    // EXISTS
    // =========================================================

    public boolean exists(
            String tenantId,
            Long listingId
    ) {

        try {

            return scheduler.checkExists(
                    buildJobKey(
                            tenantId,
                            listingId
                    )
            );

        } catch (SchedulerException e) {

            throw new IllegalStateException(
                    "Failed to check price monitor schedule",
                    e
            );
        }
    }


    // =========================================================
    // CREATE JOB DETAIL
    // =========================================================

    private JobDetail createJobDetail(
            JobKey jobKey,

            String tenantId,
            String schemaName,
            Long listingId
    ) {

        JobDataMap jobData =
                new JobDataMap();


        jobData.put(
                "tenantId",
                tenantId
        );


        jobData.put(
                "schemaName",
                schemaName
        );


        jobData.put(
                "listingId",
                listingId
        );


        return JobBuilder
                .newJob(
                        PriceMonitorJob.class
                )

                .withIdentity(
                        jobKey
                )

                .usingJobData(
                        jobData
                )

                .build();
    }

    // =========================================================
    // DAILY CRON
    // =========================================================

    private String buildDailyCron(LocalTime timeOfDay){

        if (timeOfDay == null) {

            throw new IllegalArgumentException(
                    "timeOfDay is required"
            );
        }

        return String.format(
                "%d %d %d * * ?",
                timeOfDay.getSecond(),
                timeOfDay.getMinute(),
                timeOfDay.getHour()
        );
    }

    // =========================================================
    // WEEKLY CRON
    // =========================================================

    private String buildWeeklyCron(
            Set<DayOfWeek> daysOfWeek,
            LocalTime time
    ) {

        if (time == null) {

            throw new IllegalArgumentException(
                    "timeOfDay is required"
            );
        }


        String days =
                daysOfWeek
                        .stream()

                        .sorted()

                        .map(
                                this::toQuartzDay
                        )

                        .collect(
                                Collectors.joining(
                                        ","
                                )
                        );


        return String.format(
                "%d %d %d ? * %s",
                time.getSecond(),
                time.getMinute(),
                time.getHour(),
                days
        );
    }


    // =========================================================
    // MONTHLY CRON
    // =========================================================

    private String buildMonthlyCron(
            int dayOfMonth,
            LocalTime time
    ) {

        if (time == null) {

            throw new IllegalArgumentException(
                    "timeOfDay is required"
            );
        }


        return String.format(
                "%d %d %d %d * ?",
                time.getSecond(),
                time.getMinute(),
                time.getHour(),
                dayOfMonth
        );
    }


    // =========================================================
    // JAVA DAY → QUARTZ DAY
    // =========================================================

    private String toQuartzDay(
            DayOfWeek day
    ) {

        return switch (day) {

            case MONDAY ->
                    "MON";

            case TUESDAY ->
                    "TUE";

            case WEDNESDAY ->
                    "WED";

            case THURSDAY ->
                    "THU";

            case FRIDAY ->
                    "FRI";

            case SATURDAY ->
                    "SAT";

            case SUNDAY ->
                    "SUN";
        };
    }


    // =========================================================
    // DELETE CURRENT PLANNED SCHEDULE
    // =========================================================

    private void deleteExistingSchedule(
            String tenantId,
            Long listingId
    ) throws SchedulerException {

        JobKey jobKey =
                buildJobKey(
                        tenantId,
                        listingId
                );


        if (
                scheduler.checkExists(
                        jobKey
                )
        ) {

            scheduler.deleteJob(
                    jobKey
            );
        }
    }


    // =========================================================
    // KEYS
    // =========================================================

    private JobKey buildJobKey(
            String tenantId,
            Long listingId
    ) {

        return JobKey.jobKey(
                "price-monitor-"
                        + tenantId
                        + "-"
                        + listingId,

                "price-monitor"
        );
    }


    private TriggerKey buildTriggerKey(
            String tenantId,
            Long listingId
    ) {

        return TriggerKey.triggerKey(
                "price-monitor-trigger-"
                        + tenantId
                        + "-"
                        + listingId,

                "price-monitor"
        );
    }
}