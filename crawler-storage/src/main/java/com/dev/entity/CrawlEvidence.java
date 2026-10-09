package com.dev.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "crawl_evidences"
)
@Getter
@Setter
public class CrawlEvidence {


    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "price_snapshot_id",
            nullable = false
    )
    private Long priceSnapshotId;


    @Column(
            name = "competitor_listing_id",
            nullable = false
    )
    private Long competitorListingId;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "evidence_type",
            nullable = false
    )
    private CrawlEvidenceType evidenceType;


    @Column(
            name = "bucket_name",
            nullable = false
    )
    private String bucketName;


    @Column(
            name = "object_key",
            nullable = false,
            length = 1024
    )
    private String objectKey;


    @Column(
            name = "content_type"
    )
    private String contentType;


    @Column(
            name = "size_bytes",
            nullable = false
    )
    private Long sizeBytes;


    @Column(
            name = "etag"
    )
    private String etag;


    @Column(
            name = "checksum_sha256",
            length = 64
    )
    private String checksumSha256;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;
}