package com.dev.entity;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ScraperResponse {
    private String productId;
    private String title;
    private String url;
    private BigDecimal currentPrice;
    private Map<String, Object> rawData;
}
