package com.dev.engine;

import com.dev.dto.PriceResultResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockPriceScraper
        implements PriceScraper {

    @Override
    public boolean supports(String platform) {
        return "MOCK".equalsIgnoreCase(platform);
    }

    @Override
    public PriceResultResponse fetchPrice(String url) {

        return PriceResultResponse.builder()

                .regularPrice(
                        new BigDecimal("100000")
                )

                .salePrice(
                        new BigDecimal("93000")
                )

                .effectivePrice(
                        new BigDecimal("93000")
                )

                .inStock(true)

                .promotionText(
                        "Mock sale 7%"
                )

                .available(true)

                .build();
    }
}