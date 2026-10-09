package com.dev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
public record PriceResultResponse (

    BigDecimal regularPrice,

    BigDecimal salePrice,

    BigDecimal effectivePrice,

    Boolean inStock,

    String promotionText,

    boolean available,

    ScrapeEvidence evidence
) {
}