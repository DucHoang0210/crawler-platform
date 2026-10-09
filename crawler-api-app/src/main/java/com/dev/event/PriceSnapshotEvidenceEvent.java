package com.dev.event;


public record PriceSnapshotEvidenceEvent(

        String schemaName,

        Long snapshotId,

        Long listingId,

        String rawPayload,

        String contentType

) {
}