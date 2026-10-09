ALTER TABLE crawl_evidences

    ADD CONSTRAINT uq_crawl_evidence_snapshot_type

        UNIQUE (
                price_snapshot_id,
                evidence_type
            );