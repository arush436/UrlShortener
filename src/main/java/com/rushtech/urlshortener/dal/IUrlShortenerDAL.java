package com.rushtech.urlshortener.dal;

public interface IUrlShortenerDAL {
    String getOriginalUrl(String token);

    long getOriginalUrlId(String longUrl);

    void updateToken(String token, long originalUrlId);

    long insertOriginalUrl(String longUrl);

    String getExistingOriginalUrl(String longUrl);

    void insertUrlMapping(String token, long originalUrlId);

    boolean deleteShortUrl(String token);
}