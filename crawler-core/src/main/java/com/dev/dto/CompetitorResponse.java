package com.dev.dto;

import java.math.BigDecimal;

public record CompetitorResponse(

        Long id,

        Long productId,

        String platform,

        String competitorName,

        String productUrl,

        BigDecimal alertThresholdPercent,

        BigDecimal lastEffectivePrice,

        Boolean active

) {
}