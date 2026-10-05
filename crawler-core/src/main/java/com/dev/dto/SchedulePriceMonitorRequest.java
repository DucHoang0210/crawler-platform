package com.dev.dto;

import com.dev.domain.PriceMonitorScheduleMode;
import com.dev.domain.RecurrenceType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public record SchedulePriceMonitorRequest(

        PriceMonitorScheduleMode mode,

        // =========================
        // RUN_ONCE
        // =========================
        LocalDateTime runAt,

        // =========================
        // RECURRING
        // =========================
        LocalDateTime startAt,

        LocalDateTime endAt,

        RecurrenceType recurrenceType,

        Set<DayOfWeek> daysOfWeek,

        Integer dayOfMonth,

        LocalTime timeOfDay,

        // VD:
        // Asia/Ho_Chi_Minh
        String timezone

) {
}