package com.dev.repository;

import com.dev.domain.CompetitorListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetitorListingRepository
        extends JpaRepository<
        CompetitorListing,
        Long
        > {

    List<CompetitorListing>
    findByProductId(
            Long productId
    );


    List<CompetitorListing>
    findByProductIdAndActiveTrue(
            Long productId
    );
}