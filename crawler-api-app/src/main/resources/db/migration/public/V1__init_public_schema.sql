CREATE TABLE IF NOT EXISTS public.tenants (
                                              id BIGSERIAL PRIMARY KEY,
                                              tenant_id VARCHAR(50) NOT NULL UNIQUE,
                                              company_name VARCHAR(255) NOT NULL,
                                              schema_name VARCHAR(60) NOT NULL UNIQUE,
                                              subscription_plan VARCHAR(50) NOT NULL,
                                              status VARCHAR(50) NOT NULL,
                                              created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);