package com.rushtech.urlshortener.service;

public interface IUrlShortenerService {
    String getOriginalUrl(String shortCode);
    String shortenUrl(String longUrl);
}