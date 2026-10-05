CREATE TABLE IF NOT EXISTS public.tenants (
                                              id BIGSERIAL PRIMARY KEY,
                                              tenant_id VARCHAR(50) NOT NULL UNIQUE,
                                              company_name VARCHAR(255) NOT NULL,
                                              schema_name VARCHAR(60) NOT NULL UNIQUE,
                                              subscription_plan VARCHAR(50) NOT NULL,
                                              status VARCHAR(50) NOT NULL,
                                              created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.job_configs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    target_url VARCHAR(1000) NOT NULL,
    cron_expression VARCHAR(50) NOT NULL,
    css_selector VARCHAR(500),
    xpath_expression VARCHAR(500),
    use_headless_browser BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.job_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    records_fetched INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    CONSTRAINT fk_job_execution_logs_job_id FOREIGN KEY (job_id) REFERENCES public.job_configs (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_job_execution_logs_job_id ON public.job_execution_logs(job_id);
CREATE INDEX IF NOT EXISTS idx_job_execution_logs_start_time ON public.job_execution_logs(start_time);