package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 50)
    private String tenantId; // Ví dụ: tenant_company_a

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "schema_name", nullable = false, unique = true, length = 60)
    private String schemaName; // Ví dụ: schema_company_a

    @Column(name = "subscription_plan", nullable = false)
    private String subscriptionPlan; // BASIC, VIP, ENTERPRISE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}