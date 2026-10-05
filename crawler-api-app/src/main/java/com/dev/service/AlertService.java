package com.dev.service;

import com.dev.domain.CompetitorListing;
import com.dev.domain.PriceAlert;
import com.dev.dto.PriceAlertResponse;
import com.dev.repository.CompetitorListingRepository;
import com.dev.repository.PriceAlertRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final PriceAlertRepository
            priceAlertRepository;

    private final CompetitorListingRepository
            competitorListingRepository;


    public List<PriceAlertResponse> findAll() {

        return priceAlertRepository
                .findAllByOrderByDetectedAtDesc()

                .stream()

                .map(
                        this::toResponse
                )

                .toList();
    }


    private PriceAlertResponse toResponse(
            PriceAlert alert
    ) {

        CompetitorListing listing =
                competitorListingRepository
                        .findById(
                                alert
                                        .getCompetitorListingId()
                        )

                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Competitor listing not found for alert "
                                                + alert.getId()
                                )
                        );


        return new PriceAlertResponse(

                alert.getId(),

                alert.getCompetitorListingId(),

                alert.getPriceSnapshotId(),

                listing.getPlatform(),

                listing.getCompetitorName(),

                alert.getAlertType(),

                alert.getOldPrice(),

                alert.getNewPrice(),

                // entity changePercent
                // API gọi dropPercent
                alert.getChangePercent(),

                alert.getThresholdPercent(),

                alert.getStatus(),

                // entity detectedAt
                // API gọi createdAt
                alert.getDetectedAt()
        );
    }
}