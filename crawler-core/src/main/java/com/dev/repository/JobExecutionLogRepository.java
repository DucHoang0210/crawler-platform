package com.dev.repository;

import com.dev.domain.JobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
}
