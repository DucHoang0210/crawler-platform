package com.dev.service;

import com.dev.domain.Tenant;
import com.dev.domain.TenantStatus;
import com.dev.repository.TenantRepository;
import com.dev.flyway.DynamicFlywayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantRegistrationService {

    private final TenantRepository tenantRepository;
    private final DynamicFlywayService dynamicFlywayService;

    @Transactional
    public Tenant registerTenant(String companyName, String subscriptionPlan) {
        // Tự động tạo tenantId dạng slug: "Cong Ty A" -> "tenant_cong_ty_a"
        String rawTenantId = companyName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        String tenantId = "tenant_" + rawTenantId;
        String schemaName = "schema_" + rawTenantId;

        if (tenantRepository.existsByTenantId(tenantId)) {
            throw new IllegalArgumentException("Tenant with ID '" + tenantId + "' already exists!");
        }

        // 1. Chạy Dynamic Flyway sinh Schema và Tables mới
        dynamicFlywayService.initTenantSchema(schemaName);

        // 2. Lưu thông tin Tenant vào Schema public
        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .companyName(companyName)
                .schemaName(schemaName)
                .subscriptionPlan(subscriptionPlan)
                .status(TenantStatus.ACTIVE)
                .build();

        return tenantRepository.save(tenant);
    }
}