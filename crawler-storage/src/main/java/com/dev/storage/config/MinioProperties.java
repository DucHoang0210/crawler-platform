package com.dev.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@ConfigurationProperties(
        prefix = "crawler.storage.minio"
)
public record MinioProperties(

        boolean enabled,

        String endpoint,

        String accessKey,

        String secretKey,

        Duration presignedUrlTtl,

        Buckets buckets
) {

    public record Buckets(

            String evidence,

            String reports,

            String imports
    ) {
    }
}