package com.dev.source;

import com.dev.dto.PriceResultResponse;
import com.dev.dto.ScrapeEvidence;
import com.dev.engine.PriceResult;
import com.dev.engine.PriceScraper;

import java.math.BigDecimal;

public class LazadaPriceScraper implements PriceScraper {
    @Override
    public boolean supports(String source) {
        return "LAZADA".equalsIgnoreCase(source);
    }

    @Override
    public PriceResultResponse fetchPrice(String url) {
        // Implement the logic to fetch price from Lazada
        // For now, return a dummy PriceResult
        return new PriceResultResponse(
                new BigDecimal("150000"), // regularPrice
                new BigDecimal("120000"), // salePrice
                new BigDecimal("120000"), // effectivePrice
                true, // inStock
                "Lazada sale 20%", // promotionText
                true, // available
                new ScrapeEvidence(
                        "ScrapeEnvidence Payload",
                        "ScrapeEnvidence ContenType"
                )
        );
    }
}
