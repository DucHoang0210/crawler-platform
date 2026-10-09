package com.dev.engine;

import com.dev.dto.PriceResultResponse;
import com.dev.dto.ScrapeEvidence;
import com.dev.repository.DemoProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DemoPriceScraper
        implements PriceScraper {

    private final DemoProductRepository
            demoProductRepository;


    @Override
    public boolean supports(
            String platform
    ) {

        return "DEMO"
                .equalsIgnoreCase(
                        platform
                );
    }


    @Override
    public PriceResultResponse fetchPrice(String url) {

        BigDecimal regularPrice =
                new BigDecimal("899000");

        BigDecimal salePrice = null;

        BigDecimal effectivePrice = regularPrice;

        String rawHtml =
                """
                <html>
                    <body>
                        <h1>Demo Product</h1>
                        <div class="price">899000</div>
                    </body>
                </html>
                """;

        ScrapeEvidence evidence = new ScrapeEvidence(
                rawHtml,
                "text/html; charset=UTF-8"
        );

        return new PriceResultResponse(
                regularPrice,
                salePrice,
                effectivePrice,
                Boolean.TRUE,
                "Demo Product",
                true,
                evidence
        );
    }


    private UUID extractProductId(
            String url
    ) {

        try {

            URI uri =
                    URI.create(url);

            String path =
                    uri.getPath();

            String[] parts =
                    path.split("/");

            String id =
                    parts[
                            parts.length - 1
                            ];

            return UUID.fromString(
                    id
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid DEMO product URL: "
                            + url
            );
        }
    }
}