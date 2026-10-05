package com.dev.service;

import com.dev.domain.CompetitorListing;
import com.dev.dto.CompetitorResponse;
import com.dev.dto.CreateCompetitorRequest;
import com.dev.repository.CompetitorListingRepository;
import com.dev.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final ProductRepository
            productRepository;

    private final CompetitorListingRepository
            competitorListingRepository;


    // =========================================================
    // CREATE COMPETITOR LISTING
    // =========================================================

    public CompetitorResponse create(
            Long productId,
            CreateCompetitorRequest request
    ) {

        // -----------------------------------------------------
        // 1. Kiểm tra product của mình có tồn tại không
        // -----------------------------------------------------

        if (
                !productRepository
                        .existsById(
                                productId
                        )
        ) {

            throw new IllegalArgumentException(
                    "Product not found: "
                            + productId
            );
        }


        // -----------------------------------------------------
        // 2. Tạo competitor listing
        // -----------------------------------------------------

        CompetitorListing listing =
                CompetitorListing
                        .builder()

                        .productId(
                                productId
                        )

                        .platform(
                                request
                                        .platform()
                                        .trim()
                                        .toUpperCase()
                        )

                        .competitorName(
                                request
                                        .competitorName()
                                        .trim()
                        )

                        .productUrl(
                                request
                                        .productUrl()
                                        .trim()
                        )

                        .alertThresholdPercent(
                                request
                                        .alertThresholdPercent()
                        )

                        .active(
                                true
                        )

                        .build();


        // -----------------------------------------------------
        // 3. Save
        // -----------------------------------------------------

        CompetitorListing saved =
                competitorListingRepository
                        .save(
                                listing
                        );


        // -----------------------------------------------------
        // 4. Response
        // -----------------------------------------------------

        return toResponse(
                saved
        );
    }


    // =========================================================
    // GET COMPETITORS BY PRODUCT
    // =========================================================

    public List<CompetitorResponse>
    findByProduct(
            Long productId
    ) {

        if (
                !productRepository
                        .existsById(
                                productId
                        )
        ) {

            throw new IllegalArgumentException(
                    "Product not found: "
                            + productId
            );
        }


        return competitorListingRepository
                .findByProductId(
                        productId
                )

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    // =========================================================
    // FIND ONE
    // =========================================================

    public CompetitorResponse findById(
            Long id
    ) {

        CompetitorListing listing =
                competitorListingRepository
                        .findById(id)

                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Competitor listing not found: "
                                                + id
                                )
                        );


        return toResponse(
                listing
        );
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private CompetitorResponse toResponse(
            CompetitorListing listing
    ) {

        return new CompetitorResponse(

                listing.getId(),

                listing.getProductId(),

                listing.getPlatform(),

                listing.getCompetitorName(),

                listing.getProductUrl(),

                listing.getAlertThresholdPercent(),

                listing.getLastEffectivePrice(),

                listing.getActive()
        );
    }
}