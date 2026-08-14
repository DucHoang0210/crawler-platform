package com.dev.controller;

import com.dev.service.TenantRegistrationService;
import com.dev.domain.Tenant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRegistrationService tenantRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<Tenant> registerTenant(@RequestBody RegisterTenantRequest request) {
        Tenant tenant = tenantRegistrationService.registerTenant(
                request.getCompanyName(),
                request.getSubscriptionPlan()
        );
        return ResponseEntity.ok(tenant);
    }

    @Data
    public static class RegisterTenantRequest {
        private String companyName;
        private String subscriptionPlan; // BASIC, VIP, ENTERPRISE
    }
}