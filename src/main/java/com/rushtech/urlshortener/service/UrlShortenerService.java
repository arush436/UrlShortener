package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.ITokenGenerator;

public class UrlShortenerService implements IUrlShortenerService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final ITokenGenerator tokenGenerator;
    private final IUrlShortenerDAL urlShortenerDAL;

    public UrlShortenerService(ITokenGenerator tokenGenerator, IUrlShortenerDAL urlShortenerDAL) {
        this.tokenGenerator = tokenGenerator;
        this.urlShortenerDAL = urlShortenerDAL;
    }

    public String getOriginalUrl(String token) {
        return urlShortenerDAL.getOriginalUrl(token);
    }

    public String shortenUrl(String longUrl) {
        // Check if the original URL already exists
        long originalUrlId = urlShortenerDAL.getOriginalUrlId(longUrl);
        String token = tokenGenerator.generateToken();

        if (originalUrlId == -1) {
            // If the original URL doesn't exist, insert it and create a new token
            originalUrlId = urlShortenerDAL.insertOriginalUrl(longUrl);
            urlShortenerDAL.insertUrlMapping(token, originalUrlId);
        } else {
            // If the original URL exists, update the existing token
            urlShortenerDAL.updateToken(token, originalUrlId);
        }

        return BASE_URL + token;
    }
}
