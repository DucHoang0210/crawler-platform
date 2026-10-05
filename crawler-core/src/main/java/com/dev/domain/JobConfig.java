package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_configs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_job_configs_tenant_job_name", columnNames = {"tenant_id", "job_name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "target_url", nullable = false, length = 1000)
    private String targetUrl;

    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression; // Ví dụ: 0 0/15 * * * ?

    @Column(name = "css_selector", length = 500)
    private String cssSelector;

    @Column(name = "xpath_expression", length = 500)
    private String xpathExpression;

    @Column(name = "use_headless_browser")
    private Boolean useHeadlessBrowser; // true: Playwright, false: Jsoup

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.useHeadlessBrowser == null) this.useHeadlessBrowser = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
