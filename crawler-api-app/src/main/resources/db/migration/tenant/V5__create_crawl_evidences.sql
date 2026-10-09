CREATE TABLE crawl_evidences
(
    id BIGSERIAL PRIMARY KEY,

    price_snapshot_id BIGINT NOT NULL,

    competitor_listing_id BIGINT NOT NULL,

    evidence_type VARCHAR(30) NOT NULL,

    bucket_name VARCHAR(100) NOT NULL,

    object_key VARCHAR(1024) NOT NULL,

    content_type VARCHAR(100),

    size_bytes BIGINT NOT NULL,

    etag VARCHAR(255),

    checksum_sha256 VARCHAR(64),

    created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_crawl_evidence_snapshot
        FOREIGN KEY (price_snapshot_id)
            REFERENCES price_snapshots(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_crawl_evidence_listing
        FOREIGN KEY (competitor_listing_id)
            REFERENCES competitor_listings(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_crawl_evidence_object
        UNIQUE (bucket_name, object_key)
);


CREATE INDEX idx_crawl_evidence_snapshot
    ON crawl_evidences(price_snapshot_id);


CREATE INDEX idx_crawl_evidence_listing
    ON crawl_evidences(competitor_listing_id);


CREATE INDEX idx_crawl_evidence_created_at
    ON crawl_evidences(created_at);