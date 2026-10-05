package com.dev.repository;

import com.dev.domain.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceSnapshotRepository
        extends JpaRepository<PriceSnapshot, Long> {

    List<PriceSnapshot>
    findByCompetitorListingIdOrderByCrawledAtDesc(
            Long competitorListingId
    );
}