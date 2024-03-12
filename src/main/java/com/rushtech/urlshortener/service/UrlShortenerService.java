package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.ShortCodeGenerator;

public class UrlShortenerService implements IUrlShortenerService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final IUrlShortenerDAL urlShortenerDAL;

    public UrlShortenerService(IUrlShortenerDAL urlShortenerDAL) {
        this.urlShortenerDAL = urlShortenerDAL;
    }

    public String getOriginalUrl(String shortCode) {
        return urlShortenerDAL.getOriginalUrl(shortCode);
    }

    public String shortenUrl(String longUrl) {
        // Check if the original URL already exists
        long originalUrlId = urlShortenerDAL.getOriginalUrlId(longUrl);
        String shortCode = ShortCodeGenerator.generateShortCode();

        if (originalUrlId == -1) {
            // If the original URL doesn't exist, insert it and create a new short code
            originalUrlId = urlShortenerDAL.insertOriginalUrl(longUrl);
            urlShortenerDAL.insertUrlMapping(shortCode, originalUrlId);
        } else {
            // If the original URL exists, update the existing short code
            urlShortenerDAL.updateShortCode(shortCode, originalUrlId);
        }

        return BASE_URL + shortCode;
    }
}
