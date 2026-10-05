package com.dev.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResultResponse {

    private BigDecimal regularPrice;

    private BigDecimal salePrice;

    private BigDecimal effectivePrice;

    private Boolean inStock;

    private String promotionText;

    private boolean available;
}