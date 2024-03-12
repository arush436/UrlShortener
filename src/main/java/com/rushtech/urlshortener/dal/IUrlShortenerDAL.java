package com.rushtech.urlshortener.dal;

public interface IUrlShortenerDAL {
    String getOriginalUrl(String shortCode);

    long getOriginalUrlId(String longUrl);

    void updateShortCode(String shortCode, long originalUrlId);

    long insertOriginalUrl(String longUrl);

    String getExistingOriginalUrl(String longUrl);

    void insertUrlMapping(String shortCode, long originalUrlId);
}