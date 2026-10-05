package com.dev.repository;

import com.dev.domain.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository
        extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert>
    findAllByOrderByDetectedAtDesc();


    List<PriceAlert>
    findByCompetitorListingIdOrderByDetectedAtDesc(
            Long competitorListingId
    );
}