package com.dev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        String sku,

        @NotBlank
        String productName,

        @NotNull
        @PositiveOrZero
        BigDecimal ownPrice

) {
}