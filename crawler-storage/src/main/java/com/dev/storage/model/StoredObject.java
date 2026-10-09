package com.dev.storage.model;


public record StoredObject(

        String bucket,

        String objectKey,

        String etag,

        long size,

        String contentType
) {
}