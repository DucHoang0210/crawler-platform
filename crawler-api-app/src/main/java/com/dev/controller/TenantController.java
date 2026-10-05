package com.dev.controller;

import com.dev.config.OpenApiConfig;
import com.dev.domain.Tenant;
import com.dev.domain.User;
import com.dev.dto.TenantRequest;
import com.dev.service.TenantService;
import com.dev.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for register, update, and delete tenants")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

    // =========================
    // 1. CREATE TENANT
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(
            @Valid @RequestBody TenantRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        User currentUser =
                userService.findByUsername(username);

        Tenant tenant =
                tenantService.registerTenant(request, currentUser);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Register tenant successfully",
                        "tenantId", tenant.getTenantId(),
                        "companyName", tenant.getCompanyName(),
                        "schemaName", tenant.getSchemaName()
                )
        );
    }

    // =========================
    // 2. GET ALL TENANTS
    // =========================
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {

        List<Tenant> tenants =
                tenantService.getAllTenants();

        return ResponseEntity.ok(tenants);
    }

    // =========================
    // 3. GET TENANT BY DATABASE ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(
            @PathVariable Long id
    ) {

        Tenant tenant =
                tenantService.getTenantById(id);

        return ResponseEntity.ok(tenant);
    }

    // =========================
    // 4. GET TENANT BY tenantId
    // =========================
    @GetMapping("/code/{tenantId}")
    public ResponseEntity<Tenant> getTenantByTenantId(
            @PathVariable String tenantId
    ) {

        Tenant tenant =
                tenantService.getTenantByTenantId(tenantId);

        return ResponseEntity.ok(tenant);
    }

    // =========================
    // 5. GET TENANTS OF CURRENT USER
    // =========================
    @GetMapping("/my-tenants")
    public ResponseEntity<List<Tenant>> getMyTenants(
            Authentication authentication
    ) {

        String username = authentication.getName();

        User currentUser =
                userService.findByUsername(username);

        List<Tenant> tenants =
                tenantService.getTenantsByUser(currentUser);

        return ResponseEntity.ok(tenants);
    }

    // =========================
    // 6. UPDATE TENANT
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        User currentUser =
                userService.findByUsername(username);

        Tenant tenant =
                tenantService.updateTenant(
                        id,
                        request,
                        currentUser
                );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Tenant updated successfully",
                        "tenantId", tenant.getTenantId(),
                        "companyName", tenant.getCompanyName()
                )
        );
    }

    // =========================
    // 7. DELETE TENANT
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTenant(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String username = authentication.getName();

        User currentUser =
                userService.findByUsername(username);

        tenantService.deleteTenant(
                id,
                currentUser
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Tenant deleted successfully"
                )
        );
    }
}