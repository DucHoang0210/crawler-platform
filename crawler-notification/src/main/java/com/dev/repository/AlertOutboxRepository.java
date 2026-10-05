package com.dev.repository;

import com.dev.domain.AlertOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertOutboxRepository
        extends JpaRepository<AlertOutbox, Long> {

    @Query("""
        SELECT a
        FROM AlertOutbox a
        WHERE a.status = 'PENDING'
          AND (
                a.nextRetryAt IS NULL
                OR a.nextRetryAt <= :now
              )
        ORDER BY a.createdAt ASC
    """)
    List<AlertOutbox> findPending(
            @Param("now")
            LocalDateTime now
    );
}