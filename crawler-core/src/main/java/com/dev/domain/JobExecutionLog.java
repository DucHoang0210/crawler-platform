package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "records_fetched")
    private Integer recordsFetched;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status; // SUCCESS, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
