package com.dev.service;

import com.dev.domain.Tenant;
import com.dev.domain.TenantStatus;
import com.dev.domain.TenantUser;
import com.dev.domain.User;
import com.dev.dto.TenantRequest;
import com.dev.repository.TenantRepository;
import com.dev.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TenantMigrationService tenantMigrationService;

    // =========================================================
    // 1. CREATE TENANT
    // =========================================================

    /*
     * Không đặt @Transactional ở đây.
     *
     * Lý do:
     * CREATE SCHEMA + Flyway migration có thể sử dụng
     * các connection khác nhau.
     *
     * Nếu CREATE SCHEMA chưa commit thì Flyway có thể
     * không nhìn thấy schema vừa tạo.
     */
    public Tenant registerTenant(
            TenantRequest request,
            User currentUser
    ) {

        // -----------------------------------------------------
        // Validate request
        // -----------------------------------------------------
        if (request == null) {
            throw new IllegalArgumentException(
                    "Tenant request is required"
            );
        }

        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException(
                    "Current user is required"
            );
        }

        if (request.getCompanyName() == null
                || request.getCompanyName().isBlank()) {

            throw new IllegalArgumentException(
                    "Company name is required"
            );
        }

        // -----------------------------------------------------
        // Generate tenantId + schemaName
        // -----------------------------------------------------
        String slug = createSlug(
                request.getCompanyName()
        );

        String businessTenantId =
                "tenant_" + slug;

        String schemaName =
                "schema_" + slug;

        // -----------------------------------------------------
        // Check duplicate
        // -----------------------------------------------------
        if (tenantRepository.existsByTenantId(
                businessTenantId
        )) {

            throw new IllegalArgumentException(
                    "Tenant already exists: "
                            + businessTenantId
            );
        }

        Tenant savedTenant = null;

        try {

            // =================================================
            // A. CREATE TENANT RECORD
            // =================================================
            Tenant tenant = new Tenant();

            tenant.setTenantId(
                    businessTenantId
            );

            tenant.setCompanyName(
                    request.getCompanyName().trim()
            );

            tenant.setSchemaName(
                    schemaName
            );

            tenant.setSubscriptionPlan(
                    request.getSubscriptionPlan() != null
                            && !request
                            .getSubscriptionPlan()
                            .isBlank()
                            ? request
                            .getSubscriptionPlan()
                            .trim()
                            .toUpperCase(Locale.ROOT)
                            : "BASIC"
            );

            tenant.setStatus(
                    TenantStatus.ACTIVE
            );

            /*
             * Không cần setCreatedAt() nếu Tenant đã có @PrePersist:
             *
             * @PrePersist
             * protected void onCreate() {
             *     createdAt = LocalDateTime.now();
             * }
             */

            savedTenant =
                    tenantRepository.saveAndFlush(
                            tenant
                    );

            // =================================================
            // B. CREATE DATABASE SCHEMA
            // =================================================
            createSchema(schemaName);

            // =================================================
            // C. RUN FLYWAY MIGRATION
            // =================================================
            tenantMigrationService.migrate(
                    schemaName
            );

            // =================================================
            // D. ASSIGN CURRENT USER AS OWNER
            // =================================================
            TenantUser tenantUser =
                    new TenantUser();

            tenantUser.setUserId(
                    currentUser.getId()
            );

            tenantUser.setTenantId(
                    savedTenant.getId()
            );

            tenantUser.setRole(
                    "OWNER"
            );

            tenantUserRepository.save(
                    tenantUser
            );

            // =================================================
            // E. RETURN TENANT
            // =================================================
            return savedTenant;

        } catch (Exception e) {

            // =================================================
            // CLEANUP IF PROVISIONING FAILED
            // =================================================

            // Delete schema if it was created
            try {
                dropSchema(schemaName);
            } catch (Exception ignored) {
                // Có thể log sau
            }

            // Delete tenant + relations if created
            if (savedTenant != null) {

                try {
                    tenantUserRepository
                            .deleteByTenantId(
                                    savedTenant.getId()
                            );
                } catch (Exception ignored) {
                }

                try {
                    tenantRepository.deleteById(
                            savedTenant.getId()
                    );
                } catch (Exception ignored) {
                }
            }

            throw new IllegalStateException(
                    "Failed to create tenant: "
                            + businessTenantId,
                    e
            );
        }
    }

    // =========================================================
    // 2. GET ALL TENANTS
    // =========================================================
    public List<Tenant> getAllTenants() {

        return tenantRepository.findAll();
    }

    // =========================================================
    // 3. GET TENANT BY DATABASE ID
    // =========================================================
    public Tenant getTenantById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Tenant id is required"
            );
        }

        return tenantRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found with id: "
                                        + id
                        )
                );
    }

    // =========================================================
    // 4. GET TENANT BY BUSINESS TENANT ID
    // VD: tenant_hoang_duc
    // =========================================================
    public Tenant getTenantByTenantId(
            String tenantId
    ) {

        if (tenantId == null
                || tenantId.isBlank()) {

            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }

        return tenantRepository
                .findByTenantId(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found: "
                                        + tenantId
                        )
                );
    }

    // =========================================================
    // 5. GET TENANTS OF CURRENT USER
    // =========================================================
    public List<Tenant> getTenantsByUser(
            User user
    ) {

        if (user == null
                || user.getId() == null) {

            throw new IllegalArgumentException(
                    "User is required"
            );
        }

        List<TenantUser> tenantUsers =
                tenantUserRepository
                        .findByUserId(
                                user.getId()
                        );

        return tenantUsers.stream()
                .map(tenantUser ->
                        tenantRepository
                                .findById(
                                        tenantUser
                                                .getTenantId()
                                )
                                .orElse(null)
                )
                .filter(tenant ->
                        tenant != null
                )
                .toList();
    }

    // =========================================================
    // 6. UPDATE TENANT
    // =========================================================
    @Transactional
    public Tenant updateTenant(
            Long tenantDbId,
            TenantRequest request,
            User currentUser
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Tenant request is required"
            );
        }

        Tenant tenant =
                tenantRepository
                        .findById(tenantDbId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tenant not found"
                                )
                        );

        TenantUser tenantUser =
                getTenantUser(
                        currentUser,
                        tenant
                );

        if (!"OWNER".equalsIgnoreCase(
                tenantUser.getRole()
        )) {

            throw new IllegalArgumentException(
                    "Only OWNER can update tenant"
            );
        }

        // Company name có thể đổi
        // nhưng tenantId/schemaName KHÔNG đổi
        if (request.getCompanyName() != null
                && !request
                .getCompanyName()
                .isBlank()) {

            tenant.setCompanyName(
                    request
                            .getCompanyName()
                            .trim()
            );
        }

        if (request.getSubscriptionPlan() != null
                && !request
                .getSubscriptionPlan()
                .isBlank()) {

            tenant.setSubscriptionPlan(
                    request
                            .getSubscriptionPlan()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        return tenantRepository.save(
                tenant
        );
    }

    // =========================================================
    // 7. DELETE TENANT
    // =========================================================
    @Transactional
    public void deleteTenant(
            Long tenantDbId,
            User currentUser
    ) {

        Tenant tenant =
                tenantRepository
                        .findById(tenantDbId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tenant not found"
                                )
                        );

        TenantUser tenantUser =
                getTenantUser(
                        currentUser,
                        tenant
                );

        if (!"OWNER".equalsIgnoreCase(
                tenantUser.getRole()
        )) {

            throw new IllegalArgumentException(
                    "Only OWNER can delete tenant"
            );
        }

        // Xóa mapping user <-> tenant
        tenantUserRepository
                .deleteByTenantId(
                        tenant.getId()
                );

        // Xóa schema và toàn bộ dữ liệu tenant
        dropSchema(
                tenant.getSchemaName()
        );

        // Xóa tenant public record
        tenantRepository.delete(
                tenant
        );
    }

    // =========================================================
    // GET TENANT USER
    // =========================================================
    private TenantUser getTenantUser(
            User currentUser,
            Tenant tenant
    ) {

        if (currentUser == null
                || currentUser.getId() == null) {

            throw new IllegalArgumentException(
                    "Current user is required"
            );
        }

        return tenantUserRepository
                .findByUserIdAndTenantId(
                        currentUser.getId(),
                        tenant.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "You do not belong to this tenant"
                        )
                );
    }

    // =========================================================
    // CREATE DATABASE SCHEMA
    // =========================================================
    private void createSchema(
            String schemaName
    ) {

        validateSchemaName(
                schemaName
        );

        jdbcTemplate.execute(
                "CREATE SCHEMA IF NOT EXISTS "
                        + schemaName
        );
    }

    // =========================================================
    // DROP DATABASE SCHEMA
    // =========================================================
    private void dropSchema(
            String schemaName
    ) {

        validateSchemaName(
                schemaName
        );

        jdbcTemplate.execute(
                "DROP SCHEMA IF EXISTS "
                        + schemaName
                        + " CASCADE"
        );
    }

    // =========================================================
    // VALIDATE SCHEMA NAME
    // =========================================================
    private void validateSchemaName(
            String schemaName
    ) {

        if (schemaName == null
                || !schemaName.matches(
                "^schema_[a-z0-9_]+$"
        )) {

            throw new IllegalArgumentException(
                    "Invalid schema name: "
                            + schemaName
            );
        }
    }

    // =========================================================
    // CREATE SLUG
    // =========================================================
    private String createSlug(
            String companyName
    ) {

        String normalized =
                Normalizer.normalize(
                        companyName,
                        Normalizer.Form.NFD
                );

        normalized = normalized
                .replaceAll("\\p{M}", "")
                .replace("Đ", "D")
                .replace("đ", "d");

        String slug = normalized
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll(
                        "[^a-z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid company name"
            );
        }

        return slug;
    }
}