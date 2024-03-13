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
        String originalUrl = getCachedOriginalUrl(token);
        if (originalUrl == null) {
            originalUrl = urlShortenerDAL.getOriginalUrl(token);
            cacheOriginalUrl(token, originalUrl);
        }
        return originalUrl;
    }

    @Override
    public String shortenUrl(String longUrl) {
        long originalUrlId = urlShortenerDAL.getOriginalUrlId(longUrl);
        String token = generateOrRetrieveToken(longUrl, originalUrlId);
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

    private String getCachedOriginalUrl(String token) {
        return originalUrlCache.getIfPresent(token);
    }

    private void cacheOriginalUrl(String token, String originalUrl) {
        if (originalUrl != null) {
            originalUrlCache.put(token, originalUrl);
        }
    }

    private String generateOrRetrieveToken(String longUrl, long originalUrlId) {
        String token;
        if (originalUrlId == -1) {
            token = tokenGenerator.generateToken();
            originalUrlId = urlShortenerDAL.insertOriginalUrl(longUrl);
            urlShortenerDAL.insertUrlMapping(token, originalUrlId);
        } else {
            token = urlShortenerDAL.getTokenForOriginalUrl(originalUrlId);
            if (token == null) {
                token = tokenGenerator.generateToken();
                urlShortenerDAL.insertUrlMapping(token, originalUrlId);
            }
        }
        return token;
    }
}
