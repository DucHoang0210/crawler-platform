package com.dev.storage.minio;


import com.dev.storage.config.MinioProperties;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class MinioBucketInitializer
        implements ApplicationRunner {


    private final MinioClient minioClient;

    private final MinioProperties properties;


    @Override
    public void run(
            ApplicationArguments args
    ) {

        if (
                !properties.enabled()
        ) {

            log.info(
                    "MinIO storage disabled."
            );

            return;
        }


        ensureBucket(
                properties
                        .buckets()
                        .evidence()
        );


        ensureBucket(
                properties
                        .buckets()
                        .reports()
        );


        ensureBucket(
                properties
                        .buckets()
                        .imports()
        );


        log.info(
                "MinIO storage initialized."
        );
    }


    private void ensureBucket(
            String bucket
    ) {

        try {

            boolean exists =
                    minioClient.bucketExists(

                            BucketExistsArgs
                                    .builder()

                                    .bucket(
                                            bucket
                                    )

                                    .build()
                    );


            if (
                    !exists
            ) {

                minioClient.makeBucket(

                        MakeBucketArgs
                                .builder()

                                .bucket(
                                        bucket
                                )

                                .build()
                );


                log.info(
                        "MinIO bucket created. bucket={}",
                        bucket
                );
            }


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot initialize MinIO bucket: "
                            + bucket,
                    e
            );
        }
    }
}