package com.dev.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDemoPriceRequest {

    private BigDecimal currentPrice;
}