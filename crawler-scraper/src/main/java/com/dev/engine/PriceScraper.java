package com.dev.engine;

import com.dev.dto.PriceResultResponse;

public interface PriceScraper {
    boolean supports(String source);

    PriceResultResponse fetchPrice(String url);

}
