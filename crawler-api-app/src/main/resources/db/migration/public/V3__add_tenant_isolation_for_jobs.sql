ALTER TABLE public.job_configs
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);

UPDATE public.job_configs
SET tenant_id = 'public'
WHERE tenant_id IS NULL;

ALTER TABLE public.job_configs
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE public.job_configs
    DROP CONSTRAINT IF EXISTS uk_job_configs_tenant_job_name;

ALTER TABLE public.job_configs
    ADD CONSTRAINT uk_job_configs_tenant_job_name UNIQUE (tenant_id, job_name);

CREATE INDEX IF NOT EXISTS idx_job_configs_tenant_id ON public.job_configs(tenant_id);

ALTER TABLE public.job_execution_logs
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);

UPDATE public.job_execution_logs
SET tenant_id = 'public'
WHERE tenant_id IS NULL;

ALTER TABLE public.job_execution_logs
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_job_execution_logs_tenant_id ON public.job_execution_logs(tenant_id);
