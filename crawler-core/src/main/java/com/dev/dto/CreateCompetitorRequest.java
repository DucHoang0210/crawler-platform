package com.dev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateCompetitorRequest(

        @NotBlank
        String platform,

        @NotBlank
        String competitorName,

        @NotBlank
        String productUrl,

        @NotNull
        @Positive
        BigDecimal alertThresholdPercent

) {
}