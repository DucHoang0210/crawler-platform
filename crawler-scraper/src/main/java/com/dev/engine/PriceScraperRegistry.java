package com.dev.engine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceScraperRegistry {

    private final List<PriceScraper> scrapers;

    public PriceScraper getScraper(
            String platform
    ) {

        return scrapers.stream()

                .filter(scraper ->
                        scraper.supports(platform)
                )

                .findFirst()

                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported platform: "
                                        + platform
                        )
                );
    }
}
