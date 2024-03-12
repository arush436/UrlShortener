package com.rushtech.urlshortener.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.ITokenGenerator;

public class UrlShortenerService implements IUrlShortenerService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final ITokenGenerator tokenGenerator;
    private final IUrlShortenerDAL urlShortenerDAL;
    private final Cache<String, String> originalUrlCache;

    public UrlShortenerService(ITokenGenerator tokenGenerator, IUrlShortenerDAL urlShortenerDAL, Cache<String, String> originalUrlCache) {
        this.tokenGenerator = tokenGenerator;
        this.urlShortenerDAL = urlShortenerDAL;
        this.originalUrlCache = originalUrlCache;
    }

    @Override
    public String getOriginalUrl(String token) {
        // Check cache first
        String originalUrl = originalUrlCache.getIfPresent(token);
        if (originalUrl == null) {
            // If not found in cache, retrieve from database
            originalUrl = urlShortenerDAL.getOriginalUrl(token);
            // Cache the result
            if (originalUrl != null) {
                originalUrlCache.put(token, originalUrl);
            }
        }
        return originalUrl;
    }

    @Override
    public String shortenUrl(String longUrl) {
        // Check if the original URL already exists
        long originalUrlId = urlShortenerDAL.getOriginalUrlId(longUrl);
        String token;

        if (originalUrlId == -1) {
            // If the original URL doesn't exist, insert it and create a new token
            token = tokenGenerator.generateToken();
            originalUrlId = urlShortenerDAL.insertOriginalUrl(longUrl);
            urlShortenerDAL.insertUrlMapping(token, originalUrlId);
        } else {
            // If the original URL exists, check if it already has a token
            token = urlShortenerDAL.getTokenForOriginalUrl(originalUrlId);
            if (token == null) {
                // If it doesn't have a token, generate one
                token = tokenGenerator.generateToken();
                urlShortenerDAL.insertUrlMapping(token, originalUrlId);
            }
        }

        return BASE_URL + token;
    }

    @Override
    public boolean deleteShortUrl(String token) {
        return urlShortenerDAL.deleteShortUrl(token);
    }

    @Override
    public void incrementRedirectCount(String originalUrl) {
        urlShortenerDAL.incrementRedirectCount(originalUrl);
    }
}
