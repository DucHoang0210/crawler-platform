package com.dev.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductResponse(

        Long id,

        String sku,

        String productName,

        BigDecimal ownPrice

) implements Serializable {
    private static final long serialVersionUID = 1L;
}