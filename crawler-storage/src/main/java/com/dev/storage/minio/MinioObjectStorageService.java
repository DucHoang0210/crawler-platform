package com.dev.storage.minio;


import com.dev.storage.ObjectStorageService;
import com.dev.storage.model.StoredObject;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.PutObjectArgs;

import io.minio.http.Method;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

import java.time.Duration;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class MinioObjectStorageService
        implements ObjectStorageService {


    private final MinioClient minioClient;


    @Override
    public StoredObject upload(
            String bucket,
            String objectKey,
            byte[] content,
            String contentType
    ) {

        try {

            var response =
                    minioClient.putObject(

                            PutObjectArgs
                                    .builder()

                                    .bucket(
                                            bucket
                                    )

                                    .object(
                                            objectKey
                                    )

                                    .stream(
                                            new ByteArrayInputStream(
                                                    content
                                            ),
                                            content.length,
                                            -1
                                    )

                                    .contentType(
                                            contentType
                                    )

                                    .build()
                    );


            log.debug(
                    "Object uploaded. bucket={}, key={}, size={}",
                    bucket,
                    objectKey,
                    content.length
            );


            return new StoredObject(
                    bucket,
                    objectKey,
                    response.etag(),
                    content.length,
                    contentType
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot upload object to MinIO. bucket="
                            + bucket
                            + ", key="
                            + objectKey,
                    e
            );
        }
    }


    @Override
    public void delete(
            String bucket,
            String objectKey
    ) {

        try {

            minioClient.removeObject(

                    RemoveObjectArgs
                            .builder()

                            .bucket(
                                    bucket
                            )

                            .object(
                                    objectKey
                            )

                            .build()
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot delete MinIO object",
                    e
            );
        }
    }


    @Override
    public String generatePresignedGetUrl(
            String bucket,
            String objectKey,
            Duration expiration
    ) {

        try {

            long seconds =
                    expiration.toSeconds();


            if (
                    seconds <= 0
                            ||
                            seconds > Integer.MAX_VALUE
            ) {

                throw new IllegalArgumentException(
                        "Invalid presigned URL expiration"
                );
            }


            return minioClient
                    .getPresignedObjectUrl(

                            GetPresignedObjectUrlArgs
                                    .builder()

                                    .method(
                                            Method.GET
                                    )

                                    .bucket(
                                            bucket
                                    )

                                    .object(
                                            objectKey
                                    )

                                    .expiry(
                                            (int) seconds,
                                            TimeUnit.SECONDS
                                    )

                                    .build()
                    );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot generate presigned URL",
                    e
            );
        }
    }


    @Override
    public boolean exists(
            String bucket,
            String objectKey
    ) {

        try {

            StatObjectResponse ignored =
                    minioClient.statObject(

                            StatObjectArgs
                                    .builder()

                                    .bucket(
                                            bucket
                                    )

                                    .object(
                                            objectKey
                                    )

                                    .build()
                    );


            return true;

        } catch (Exception e) {

            return false;
        }
    }
}