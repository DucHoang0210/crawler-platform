package com.dev.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertEvent {

    private Long alertId;

    private Long competitorListingId;

    private String platform;

    private String competitorName;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private BigDecimal dropPercent;

    private LocalDateTime observedAt;
}