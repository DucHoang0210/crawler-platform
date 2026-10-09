package com.dev.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    @NotBlank
    public String productName;

    public BigDecimal ownPrice;

}
