package com.dev.repository;

import com.dev.domain.DemoProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DemoProductRepository
        extends JpaRepository<DemoProduct, Long> {

    Optional<DemoProduct>
    findByPublicId(UUID publicId);
}