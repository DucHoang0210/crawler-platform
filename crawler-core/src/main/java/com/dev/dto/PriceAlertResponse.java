package com.dev.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceAlertResponse(

        Long id,

        Long listingId,

        Long snapshotId,

        String platform,

        String competitorName,

        String alertType,

        BigDecimal oldPrice,

        BigDecimal newPrice,

        BigDecimal dropPercent,

        BigDecimal thresholdPercent,

        String status,

        LocalDateTime createdAt

) {
}