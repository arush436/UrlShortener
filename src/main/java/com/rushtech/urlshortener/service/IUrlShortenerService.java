package com.rushtech.urlshortener.service;

public interface IUrlShortenerService {

    String getOriginalUrl(String token);

    String shortenUrl(String longUrl);

    boolean deleteShortUrl(String token);

    void incrementRedirectCount(String originalUrl);
}