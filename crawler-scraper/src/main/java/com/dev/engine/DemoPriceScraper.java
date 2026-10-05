package com.dev.engine;

import com.dev.domain.DemoProduct;
import com.dev.dto.PriceResultResponse;
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
    public PriceResultResponse fetchPrice(
            String url
    ) {

        UUID publicId =
                extractProductId(
                        url
                );


        DemoProduct product =
                demoProductRepository
                        .findByPublicId(
                                publicId
                        )

                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Demo product not found: "
                                                + publicId
                                )
                        );


        BigDecimal regularPrice =
                product.getRegularPrice();

        BigDecimal currentPrice =
                product.getCurrentPrice();


        BigDecimal salePrice = null;

        if (
                currentPrice.compareTo(
                        regularPrice
                ) < 0
        ) {

            salePrice =
                    currentPrice;
        }


        return PriceResultResponse
                .builder()

                .regularPrice(
                        regularPrice
                )

                .salePrice(
                        salePrice
                )

                .effectivePrice(
                        currentPrice
                )

                .inStock(
                        product.getInStock()
                )

                .promotionText(
                        product.getPromotionText()
                )

                .available(true)

                .build();
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