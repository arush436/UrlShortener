package com.rushtech.urlshortener.model;

public class ShortUrlResponse {

    private String shortUrl;

    public ShortUrlResponse(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
