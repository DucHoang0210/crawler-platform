package com.dev.repository;


import com.dev.entity.CrawlEvidence;
import com.dev.entity.CrawlEvidenceType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CrawlEvidenceRepository
        extends JpaRepository<CrawlEvidence, Long> {


    List<CrawlEvidence>
    findByPriceSnapshotIdOrderByCreatedAtAsc(
            Long priceSnapshotId
    );


    boolean existsByPriceSnapshotIdAndEvidenceType(

            Long priceSnapshotId,

            CrawlEvidenceType evidenceType
    );


    Optional<CrawlEvidence>
    findFirstByPriceSnapshotIdAndEvidenceType(

            Long priceSnapshotId,

            CrawlEvidenceType evidenceType
    );
}