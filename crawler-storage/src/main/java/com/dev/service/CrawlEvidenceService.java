package com.dev.service;


import com.dev.context.TenantContext;

import com.dev.entity.CrawlEvidence;
import com.dev.entity.CrawlEvidenceType;

import com.dev.repository.CrawlEvidenceRepository;

import com.dev.storage.ObjectStorageService;

import com.dev.storage.config.MinioProperties;

import com.dev.storage.model.StoredObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.HexFormat;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlEvidenceService {


    private final ObjectStorageService
            objectStorageService;


    private final CrawlEvidenceRepository
            crawlEvidenceRepository;


    private final MinioProperties
            minioProperties;


    // =========================================================
    // STORE RAW SCRAPER DATA
    // =========================================================

    public CrawlEvidence saveRawEvidence(

            Long snapshotId,

            Long listingId,

            String rawPayload,

            String contentType
    ) {

        if (
                rawPayload == null
                        ||
                        rawPayload.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Raw evidence payload is empty"
            );
        }


        CrawlEvidenceType evidenceType =
                resolveEvidenceType(
                        contentType
                );


        String extension =
                resolveExtension(
                        evidenceType
                );


        return saveTextEvidence(

                snapshotId,

                listingId,

                evidenceType,

                rawPayload,

                normalizeContentType(
                        contentType,
                        evidenceType
                ),

                extension
        );
    }


    // =========================================================
    // STORE TEXT EVIDENCE
    // =========================================================

    public CrawlEvidence saveTextEvidence(

            Long snapshotId,

            Long listingId,

            CrawlEvidenceType type,

            String content,

            String contentType,

            String extension
    ) {

        byte[] bytes =
                content.getBytes(
                        StandardCharsets.UTF_8
                );


        return saveEvidence(

                snapshotId,

                listingId,

                type,

                bytes,

                contentType,

                extension
        );
    }


    // =========================================================
    // STORE EVIDENCE
    // =========================================================

    public CrawlEvidence saveEvidence(

            Long snapshotId,

            Long listingId,

            CrawlEvidenceType type,

            byte[] content,

            String contentType,

            String extension
    ) {

        String schemaName =
                TenantContext
                        .getCurrentTenant();


        if (
                schemaName == null
                        ||
                        schemaName.isBlank()
        ) {

            throw new IllegalStateException(
                    "Tenant schema is missing"
            );
        }


        String bucket =
                minioProperties
                        .buckets()
                        .evidence();


        String objectKey =
                buildObjectKey(

                        schemaName,

                        listingId,

                        snapshotId,

                        type,

                        extension
                );


        /*
         * Optional:
         * tránh tạo metadata trùng nếu job bị retry.
         */

        boolean alreadyExists =
                crawlEvidenceRepository
                        .existsByPriceSnapshotIdAndEvidenceType(
                                snapshotId,
                                type
                        );


        if (
                alreadyExists
        ) {

            log.debug(
                    "Evidence already exists. snapshotId={}, type={}",
                    snapshotId,
                    type
            );


            return crawlEvidenceRepository
                    .findFirstByPriceSnapshotIdAndEvidenceType(
                            snapshotId,
                            type
                    )
                    .orElseThrow();
        }


        StoredObject stored =
                objectStorageService.upload(

                        bucket,

                        objectKey,

                        content,

                        contentType
                );


        try {

            CrawlEvidence evidence =
                    new CrawlEvidence();


            evidence.setPriceSnapshotId(
                    snapshotId
            );


            evidence.setCompetitorListingId(
                    listingId
            );


            evidence.setEvidenceType(
                    type
            );


            evidence.setBucketName(
                    stored.bucket()
            );


            evidence.setObjectKey(
                    stored.objectKey()
            );


            evidence.setContentType(
                    contentType
            );


            evidence.setSizeBytes(
                    stored.size()
            );


            evidence.setEtag(
                    stored.etag()
            );


            evidence.setChecksumSha256(
                    sha256(
                            content
                    )
            );


            evidence.setCreatedAt(
                    LocalDateTime.now()
            );


            CrawlEvidence saved =
                    crawlEvidenceRepository.save(
                            evidence
                    );


            log.info(
                    "Crawl evidence stored. schema={}, listingId={}, snapshotId={}, type={}, size={}",
                    schemaName,
                    listingId,
                    snapshotId,
                    type,
                    stored.size()
            );


            return saved;


        } catch (RuntimeException e) {

            /*
             * MinIO upload thành công
             * nhưng metadata DB lỗi.
             *
             * Thực hiện compensating action.
             */

            try {

                objectStorageService.delete(
                        bucket,
                        objectKey
                );

            } catch (Exception cleanupException) {

                log.error(
                        "Failed to clean orphan MinIO object. bucket={}, objectKey={}",
                        bucket,
                        objectKey,
                        cleanupException
                );
            }


            throw e;
        }
    }


    // =========================================================
    // QUERY BY SNAPSHOT
    // =========================================================

    public List<CrawlEvidence> findBySnapshotId(
            Long snapshotId
    ) {

        return crawlEvidenceRepository
                .findByPriceSnapshotIdOrderByCreatedAtAsc(
                        snapshotId
                );
    }


    // =========================================================
    // GET SINGLE EVIDENCE
    // =========================================================

    public CrawlEvidence getById(
            Long evidenceId
    ) {

        return crawlEvidenceRepository
                .findById(
                        evidenceId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Crawl evidence not found: "
                                                + evidenceId
                                )
                );
    }


    // =========================================================
    // PRESIGNED URL
    // =========================================================

    public String generatePresignedUrl(
            Long evidenceId
    ) {

        CrawlEvidence evidence =
                getById(
                        evidenceId
                );


        Duration expiration =
                minioProperties
                        .presignedUrlTtl();


        return objectStorageService
                .generatePresignedGetUrl(

                        evidence.getBucketName(),

                        evidence.getObjectKey(),

                        expiration
                );
    }


    public long getPresignedUrlTtlSeconds() {

        return minioProperties
                .presignedUrlTtl()
                .toSeconds();
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private CrawlEvidenceType resolveEvidenceType(
            String contentType
    ) {

        if (
                contentType != null
                        &&
                        contentType
                                .toLowerCase()
                                .contains(
                                        "application/json"
                                )
        ) {

            return CrawlEvidenceType.RAW_JSON;
        }


        return CrawlEvidenceType.RAW_HTML;
    }


    private String resolveExtension(
            CrawlEvidenceType type
    ) {

        return switch (
                type
                ) {

            case RAW_JSON ->
                    "json";

            case RAW_HTML ->
                    "html";

            case SCREENSHOT ->
                    "png";
        };
    }


    private String normalizeContentType(

            String contentType,

            CrawlEvidenceType type
    ) {

        if (
                contentType != null
                        &&
                        !contentType.isBlank()
        ) {

            return contentType;
        }


        return switch (
                type
                ) {

            case RAW_JSON ->
                    "application/json";

            case RAW_HTML ->
                    "text/html; charset=UTF-8";

            case SCREENSHOT ->
                    "image/png";
        };
    }


    private String buildObjectKey(

            String schemaName,

            Long listingId,

            Long snapshotId,

            CrawlEvidenceType type,

            String extension
    ) {

        return schemaName

                + "/listings/"

                + listingId

                + "/snapshots/"

                + snapshotId

                + "/"

                + type
                .name()
                .toLowerCase()

                + "."

                + extension;
    }


    private String sha256(
            byte[] content
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    content
                            )
                    );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot calculate SHA-256 checksum",
                    e
            );
        }
    }
}