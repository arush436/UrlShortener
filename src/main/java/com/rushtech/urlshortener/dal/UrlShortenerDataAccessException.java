package com.rushtech.urlshortener.dal;

public class UrlShortenerDataAccessException extends RuntimeException {

    public UrlShortenerDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
