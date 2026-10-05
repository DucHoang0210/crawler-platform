package com.dev.dto;

import java.math.BigDecimal;

public record ProductResponse(

        Long id,

        String sku,

        String productName,

        BigDecimal ownPrice

) {
}