-- This migration intentionally left blank to avoid creating tenant tables in the public schema.
-- Tenant-specific schema migrations live under: classpath:db/migration/tenant
-- See: db/migration/tenant/V2__price_intelligence_core.sql which creates competitor_products
-- (DynamicFlywayService / TenantMigrationService apply tenant migrations per-schema)

-- No DDL here to ensure competitor_products exists only inside tenant schemas.
