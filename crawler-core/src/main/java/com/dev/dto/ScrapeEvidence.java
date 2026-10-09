package com.dev.dto;

public record ScrapeEvidence(

        String rawPayload,

        String contentType

) {

    public boolean hasContent() {

        return rawPayload != null
                && !rawPayload.isBlank();
    }


    public boolean isHtml() {

        return contentType != null
                && contentType
                .toLowerCase()
                .contains("text/html");
    }


    public boolean isJson() {

        return contentType != null
                && contentType
                .toLowerCase()
                .contains("application/json");
    }
}