package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.UrlShortenerDAL;

public class UrlShortenerService {

    private final UrlShortenerDAL urlShortenerDAL;

    public UrlShortenerService(UrlShortenerDAL urlShortenerDAL) {
        this.urlShortenerDAL = urlShortenerDAL;
    }

    public String getOriginalUrl(String shortCode) {
        return urlShortenerDAL.getOriginalUrl(shortCode);
    }
}
