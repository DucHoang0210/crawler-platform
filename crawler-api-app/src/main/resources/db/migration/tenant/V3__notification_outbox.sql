-- ============================================================
-- V3 - Notification / Webhook / Outbox
-- ============================================================


-- ============================================================
-- 1. WEBHOOK CONFIG
-- ============================================================

CREATE TABLE webhook_configs
(
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    type VARCHAR(30) NOT NULL,

    target_url TEXT,

    secret_key TEXT,

    destination TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_webhook_configs_active
    ON webhook_configs(is_active);


CREATE INDEX idx_webhook_configs_type
    ON webhook_configs(type);


-- ============================================================
-- 2. ALERT OUTBOX
-- ============================================================

CREATE TABLE alert_outbox
(
    id BIGSERIAL PRIMARY KEY,

    event_id UUID NOT NULL UNIQUE,

    event_type VARCHAR(100) NOT NULL,

    payload TEXT NOT NULL,

    status VARCHAR(30)
        NOT NULL DEFAULT 'PENDING',

    retry_count INTEGER
        NOT NULL DEFAULT 0,

    next_retry_at TIMESTAMP,

    last_error TEXT,

    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    sent_at TIMESTAMP,

    CONSTRAINT chk_alert_outbox_retry
        CHECK (retry_count >= 0)
);


CREATE INDEX idx_alert_outbox_pending
    ON alert_outbox(
                    status,
                    next_retry_at
        );


CREATE INDEX idx_alert_outbox_created_at
    ON alert_outbox(
                    created_at
        );