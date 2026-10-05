package com.dev.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDemoProductRequest {

    private String name;

    private BigDecimal regularPrice;

    private BigDecimal currentPrice;
}