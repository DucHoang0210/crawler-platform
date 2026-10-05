-- ============================================================
-- V2 - Price Intelligence Core
-- ============================================================


-- ============================================================
-- 1. PRODUCTS
-- ============================================================

CREATE TABLE products
(
    id BIGSERIAL PRIMARY KEY,

    sku VARCHAR(100) NOT NULL,

    product_name VARCHAR(255) NOT NULL,

    own_price NUMERIC(19, 2) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_products_sku
        UNIQUE (sku),

    CONSTRAINT chk_products_own_price
        CHECK (own_price >= 0)
);


CREATE INDEX idx_products_sku
    ON products(sku);


-- ============================================================
-- 2. COMPETITOR LISTINGS
-- ============================================================

CREATE TABLE competitor_listings
(
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,

    platform VARCHAR(50) NOT NULL,

    competitor_name VARCHAR(255) NOT NULL,

    external_product_id VARCHAR(255),

    seller_name VARCHAR(255),

    product_url TEXT NOT NULL,

    alert_threshold_percent NUMERIC(10, 4)
        NOT NULL DEFAULT 5.0000,

    last_regular_price NUMERIC(19, 2),

    last_sale_price NUMERIC(19, 2),

    last_effective_price NUMERIC(19, 2),

    last_in_stock BOOLEAN,

    last_crawled_at TIMESTAMP,

    last_crawl_status VARCHAR(30),

    last_error TEXT,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_competitor_listing_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_competitor_alert_threshold
        CHECK (
            alert_threshold_percent >= 0
                AND alert_threshold_percent <= 100
            ),

    CONSTRAINT chk_competitor_last_regular_price
        CHECK (
            last_regular_price IS NULL
                OR last_regular_price >= 0
            ),

    CONSTRAINT chk_competitor_last_sale_price
        CHECK (
            last_sale_price IS NULL
                OR last_sale_price >= 0
            ),

    CONSTRAINT chk_competitor_last_effective_price
        CHECK (
            last_effective_price IS NULL
                OR last_effective_price >= 0
            )
);


CREATE INDEX idx_competitor_listings_product
    ON competitor_listings(product_id);

CREATE INDEX idx_competitor_listings_active
    ON competitor_listings(active);

CREATE INDEX idx_competitor_listings_platform
    ON competitor_listings(platform);


-- ============================================================
-- 3. PRICE SNAPSHOTS
-- ============================================================

CREATE TABLE price_snapshots
(
    id BIGSERIAL PRIMARY KEY,

    competitor_listing_id BIGINT NOT NULL,

    regular_price NUMERIC(19, 2),

    sale_price NUMERIC(19, 2),

    effective_price NUMERIC(19, 2) NOT NULL,

    discount_percent NUMERIC(10, 4),

    in_stock BOOLEAN,

    promotion_text TEXT,

    raw_data JSONB,

    crawled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_price_snapshot_listing
        FOREIGN KEY (competitor_listing_id)
            REFERENCES competitor_listings(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_snapshot_regular_price
        CHECK (
            regular_price IS NULL
                OR regular_price >= 0
            ),

    CONSTRAINT chk_snapshot_sale_price
        CHECK (
            sale_price IS NULL
                OR sale_price >= 0
            ),

    CONSTRAINT chk_snapshot_effective_price
        CHECK (effective_price >= 0)
);


CREATE INDEX idx_price_snapshots_listing_time
    ON price_snapshots(
                       competitor_listing_id,
                       crawled_at DESC
        );


CREATE INDEX idx_price_snapshots_crawled_at
    ON price_snapshots(
                       crawled_at DESC
        );


-- ============================================================
-- 4. CRAWL RUNS
-- ============================================================

CREATE TABLE crawl_runs
(
    id BIGSERIAL PRIMARY KEY,

    competitor_listing_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    http_status INTEGER,

    duration_ms BIGINT,

    error_message TEXT,

    started_at TIMESTAMP
                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    finished_at TIMESTAMP,

    CONSTRAINT fk_crawl_run_listing
        FOREIGN KEY (competitor_listing_id)
            REFERENCES competitor_listings(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_crawl_run_status
        CHECK (
            status IN (
                       'RUNNING',
                       'SUCCESS',
                       'FAILED',
                       'SKIPPED'
                )
            ),

    CONSTRAINT chk_crawl_run_duration
        CHECK (
            duration_ms IS NULL
                OR duration_ms >= 0
            )
);


CREATE INDEX idx_crawl_runs_listing_time
    ON crawl_runs(
                  competitor_listing_id,
                  started_at DESC
        );


CREATE INDEX idx_crawl_runs_status
    ON crawl_runs(status);


-- ============================================================
-- 5. PRICE ALERTS
-- ============================================================

CREATE TABLE price_alerts
(
    id BIGSERIAL PRIMARY KEY,

    competitor_listing_id BIGINT NOT NULL,

    price_snapshot_id BIGINT NOT NULL,

    alert_type VARCHAR(50)
                                 NOT NULL DEFAULT 'PRICE_DROP',

    old_price NUMERIC(19, 2) NOT NULL,

    new_price NUMERIC(19, 2) NOT NULL,

    change_percent NUMERIC(10, 4) NOT NULL,

    threshold_percent NUMERIC(10, 4) NOT NULL,

    status VARCHAR(50)
        NOT NULL DEFAULT 'PENDING',

    detected_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_price_alert_competitor
        FOREIGN KEY (competitor_listing_id)
            REFERENCES competitor_listings(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_price_alert_snapshot
        FOREIGN KEY (price_snapshot_id)
            REFERENCES price_snapshots(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_price_alert_old_price
        CHECK (old_price >= 0),

    CONSTRAINT chk_price_alert_new_price
        CHECK (new_price >= 0),

    CONSTRAINT chk_price_alert_change_percent
        CHECK (change_percent >= 0),

    CONSTRAINT chk_price_alert_threshold_percent
        CHECK (
            threshold_percent >= 0
                AND threshold_percent <= 100
            )
);


CREATE INDEX idx_price_alert_competitor
    ON price_alerts(
                    competitor_listing_id
        );


CREATE INDEX idx_price_alert_snapshot
    ON price_alerts(
                    price_snapshot_id
        );


CREATE INDEX idx_price_alert_status
    ON price_alerts(
                    status
        );


CREATE INDEX idx_price_alert_detected_at
    ON price_alerts(
                    detected_at DESC
        );


CREATE UNIQUE INDEX uq_price_alert_snapshot_type
    ON price_alerts(
                    price_snapshot_id,
                    alert_type
        );


-- ============================================================
-- 6. MONITORING JOB CONFIGURATION
-- ============================================================

CREATE TABLE monitoring_jobs
(
    id BIGSERIAL PRIMARY KEY,

    job_name VARCHAR(255) NOT NULL,

    competitor_listing_id BIGINT,

    quartz_job_key VARCHAR(255) UNIQUE,

    cron_expression VARCHAR(100) NOT NULL,

    timezone VARCHAR(100)
        NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',

    enabled BOOLEAN
        NOT NULL DEFAULT TRUE,

    last_run_at TIMESTAMP,

    next_run_at TIMESTAMP,

    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_monitoring_job_listing
        FOREIGN KEY (competitor_listing_id)
            REFERENCES competitor_listings(id)
            ON DELETE CASCADE
);


CREATE INDEX idx_monitoring_jobs_enabled
    ON monitoring_jobs(enabled);


CREATE INDEX idx_monitoring_jobs_listing
    ON monitoring_jobs(
                       competitor_listing_id
        );