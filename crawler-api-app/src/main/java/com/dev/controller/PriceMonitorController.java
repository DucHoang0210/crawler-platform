package com.dev.controller;

import com.dev.context.TenantContext;

import com.dev.domain.PriceMonitorScheduleMode;
import com.dev.domain.RecurrenceType;
import com.dev.dto.SchedulePriceMonitorRequest;

import com.dev.scheduler.PriceMonitorScheduler;

import com.dev.service.PriceMonitorJobExecutor;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;


@RestController
@RequestMapping(
        "/api/v1/price-monitor"
)
@RequiredArgsConstructor
public class PriceMonitorController {

    private final PriceMonitorJobExecutor
            executor;

    private final PriceMonitorScheduler
            scheduler;



    // =========================================================
    // MANUAL RUN
    // =========================================================

    @PostMapping("/{listingId}/run")
    public ResponseEntity<?> run(
            @PathVariable Long listingId
    ) {

        String schemaName =
                TenantContext.getCurrentTenant();


        executor.execute(
                schemaName,
                listingId
        );


        return ResponseEntity.ok().build();
    }


    // =========================================================
    // ADVANCED SCHEDULER
    // =========================================================

    @PostMapping(
            "/{listingId}/schedule"
    )
    public ResponseEntity<
            Map<String, Object>
            > schedule(
            @PathVariable
            Long listingId,

            @RequestBody
            SchedulePriceMonitorRequest request,

            HttpServletRequest servletRequest
    ) {

        String tenantId =
                requireTenantId(
                        servletRequest
                );


        String schemaName =
                requireSchema();


        if (request.mode() == null) {

            throw new IllegalArgumentException(
                    "Schedule mode is required"
            );
        }


        String timezone =
                request.timezone() == null
                        ||
                        request.timezone()
                                .isBlank()

                        ? "Asia/Ho_Chi_Minh"

                        : request.timezone();


        ZoneId zoneId =
                ZoneId.of(
                        timezone
                );


        ZonedDateTime now =
                ZonedDateTime.now(
                        zoneId
                );


        // =====================================================
        // RUN NOW
        // =====================================================

        if (
                request.mode()
                        ==
                        PriceMonitorScheduleMode.RUN_NOW
        ) {

            /*
             * Không schedule qua Quartz.
             *
             * Quan trọng:
             * Không xóa recurring schedule đang tồn tại.
             */
            executor.execute(
                    schemaName,
                    listingId
            );


            return ResponseEntity.ok(
                    Map.of(
                            "mode",
                            "RUN_NOW",

                            "executed",
                            true,

                            "listingId",
                            listingId
                    )
            );
        }


        // =====================================================
        // RUN ONCE
        // =====================================================

        if (
                request.mode()
                        ==
                        PriceMonitorScheduleMode.RUN_ONCE
        ) {

            if (
                    request.runAt()
                            == null
            ) {

                throw new IllegalArgumentException(
                        "runAt is required for RUN_ONCE"
                );
            }


            ZonedDateTime runAt =
                    request
                            .runAt()
                            .atZone(
                                    zoneId
                            );


            if (
                    !runAt.isAfter(
                            now
                    )
            ) {

                throw new IllegalArgumentException(
                        "runAt must be in the future"
                );
            }


            scheduler.scheduleOnce(
                    tenantId,
                    schemaName,
                    listingId,
                    runAt
            );


            return ResponseEntity.ok(
                    Map.of(
                            "mode",
                            "RUN_ONCE",

                            "scheduled",
                            true,

                            "listingId",
                            listingId,

                            "runAt",
                            request
                                    .runAt()
                                    .toString(),

                            "timezone",
                            timezone
                    )
            );
        }


        // =====================================================
        // RECURRING
        // =====================================================

        if (
                request.mode()
                        ==
                        PriceMonitorScheduleMode.RECURRING
        ) {

            if (
                    request.startAt()
                            == null
                            ||
                            request.endAt()
                                    == null
            ) {

                throw new IllegalArgumentException(
                        "startAt and endAt are required"
                );
            }


            ZonedDateTime startAt =
                    request
                            .startAt()
                            .atZone(
                                    zoneId
                            );


            ZonedDateTime endAt =
                    request
                            .endAt()
                            .atZone(
                                    zoneId
                            );


            if (
                    !startAt.isAfter(
                            now
                    )
            ) {

                throw new IllegalArgumentException(
                        "startAt must be in the future"
                );
            }


            if (
                    !endAt.isAfter(
                            startAt
                    )
            ) {

                throw new IllegalArgumentException(
                        "endAt must be after startAt"
                );
            }


            if (
                    request.timeOfDay()
                            == null
            ) {

                throw new IllegalArgumentException(
                        "timeOfDay is required"
                );
            }


            if (
                    request.recurrenceType()
                            == null
            ) {

                throw new IllegalArgumentException(
                        "recurrenceType is required"
                );
            }

            // ===============================================
            // DAILY
            // ===============================================

            if (
                    request.recurrenceType()
                            ==
                            RecurrenceType.DAILY
            ) {

                scheduler.scheduleDaily(
                        tenantId,
                        schemaName,
                        listingId,

                        startAt,
                        endAt,

                        request.timeOfDay(),

                        zoneId
                );
            }

            // ===============================================
            // WEEKLY
            // ===============================================

            if (
                    request.recurrenceType()
                            ==
                            RecurrenceType.WEEKLY
            ) {

                scheduler.scheduleWeekly(
                        tenantId,
                        schemaName,
                        listingId,

                        startAt,
                        endAt,

                        request.daysOfWeek(),

                        request.timeOfDay(),

                        zoneId
                );
            }


            // ===============================================
            // MONTHLY
            // ======================================
            // =========

            else if (
                    request.recurrenceType()
                            ==
                            RecurrenceType.MONTHLY
            ) {

                if (
                        request.dayOfMonth()
                                == null
                ) {

                    throw new IllegalArgumentException(
                            "dayOfMonth is required for MONTHLY schedule"
                    );
                }


                scheduler.scheduleMonthly(
                        tenantId,
                        schemaName,
                        listingId,

                        startAt,
                        endAt,

                        request.dayOfMonth(),

                        request.timeOfDay(),

                        zoneId
                );
            }


            return ResponseEntity.ok(
                    Map.of(
                            "mode",
                            "RECURRING",

                            "recurrenceType",
                            request
                                    .recurrenceType()
                                    .name(),

                            "scheduled",
                            true,

                            "listingId",
                            listingId,

                            "timezone",
                            timezone
                    )
            );
        }


        throw new IllegalArgumentException(
                "Unsupported schedule mode"
        );
    }


    // =========================================================
    // CANCEL PLANNED SCHEDULE
    // =========================================================

    @DeleteMapping(
            "/{listingId}/schedule"
    )
    public ResponseEntity<
            Map<String, Object>
            > cancel(
            @PathVariable
            Long listingId,

            HttpServletRequest request
    ) {

        String tenantId =
                requireTenantId(
                        request
                );


        boolean cancelled =
                scheduler.cancel(
                        tenantId,
                        listingId
                );


        return ResponseEntity.ok(
                Map.of(
                        "listingId",
                        listingId,

                        "cancelled",
                        cancelled
                )
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String requireSchema() {

        String schema =
                TenantContext
                        .getCurrentTenant();


        if (
                schema == null
                        ||
                        schema.isBlank()
        ) {

            throw new IllegalStateException(
                    "Tenant schema is not available"
            );
        }


        return schema;
    }


    private String requireTenantId(
            HttpServletRequest request
    ) {

        Object value =
                request.getAttribute(
                        "tenantId"
                );


        if (
                !(value instanceof String tenantId)
                        ||
                        tenantId.isBlank()
        ) {

            throw new IllegalStateException(
                    "Tenant ID is not available"
            );
        }


        return tenantId;
    }
}