package com.dev.repository;

import com.dev.domain.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobConfigRepository extends JpaRepository<JobConfig, Long> {
    Optional<JobConfig> findByIdAndTenantId(Long id, String tenantId);

    Optional<JobConfig> findByTenantIdAndJobName(String tenantId, String jobName);
}
