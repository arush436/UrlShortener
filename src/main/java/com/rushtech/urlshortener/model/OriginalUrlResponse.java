package com.rushtech.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OriginalUrlResponse {

    @JsonProperty("originalUrl")
    private final String originalUrl;

    public OriginalUrlResponse(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
