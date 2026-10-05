package com.dev.source;

import com.dev.dto.PriceResultResponse;
import com.dev.engine.PriceResult;
import com.dev.engine.PriceScraper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ShopeePriceScraper implements PriceScraper {
    @Override
    public boolean supports(String source) {
        return "SHOPEE".equalsIgnoreCase(source);
    }

    @Override
    public PriceResultResponse fetchPrice(String url) {

        // Sau này thay bằng crawler thật
        BigDecimal price =
                new BigDecimal("199000");

        return new PriceResultResponse(
                price, // regularPrice
                price, // salePrice
                price, // effectivePrice
                true, // inStock
                "Shopee sale 0%", // promotionText
                true // available
        );
    }
}
