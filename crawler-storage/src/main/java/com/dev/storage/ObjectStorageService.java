package com.dev.storage;


import com.dev.storage.model.StoredObject;

import java.time.Duration;


public interface ObjectStorageService {


    StoredObject upload(
            String bucket,
            String objectKey,
            byte[] content,
            String contentType
    );


    void delete(
            String bucket,
            String objectKey
    );


    String generatePresignedGetUrl(
            String bucket,
            String objectKey,
            Duration expiration
    );


    boolean exists(
            String bucket,
            String objectKey
    );
}