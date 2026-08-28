package com.dev.repository;

import com.dev.domain.DomainConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainConfigRepository extends JpaRepository<DomainConfig, Long> {
    Optional<DomainConfig> findByDomain(String domain);
    Optional<DomainConfig> findByDomainIgnoreCase(String domain);
}