package com.rushtech.urlshortener.controller;

import com.rushtech.urlshortener.dal.UrlShortenerDAL;
import com.rushtech.urlshortener.model.OriginalUrlResponse;
import com.rushtech.urlshortener.model.ShortUrlRequest;
import com.rushtech.urlshortener.model.ShortUrlResponse;
import com.rushtech.urlshortener.service.UrlShortenerService;
import org.apache.commons.validator.routines.UrlValidator;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerDAL.class);

    private static final UrlValidator urlValidator = new UrlValidator();

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    public void startServer() {
        Javalin urlShortenerApp = Javalin.create().start(8080);

        urlShortenerApp.get("/", ctx -> ctx.result("Welcome to the URL Shortener API!"));

        urlShortenerApp.get("/{shortCode}", ctx -> {
            String shortCode = ctx.pathParam("shortCode");
            String originalUrl = urlShortenerService.getOriginalUrl(shortCode);
            if (originalUrl != null) {
                ctx.redirect(originalUrl);
            } else {
                ctx.status(404).result("Shortened URL not found");
            }
        });

        urlShortenerApp.get("/original/{shortCode}", ctx -> {
            String shortCode = ctx.pathParam("shortCode");
            try {
                String originalUrl = urlShortenerService.getOriginalUrl(shortCode);

                if (originalUrl != null) {
                    ctx.json(new OriginalUrlResponse(originalUrl));
                } else {
                    ctx.status(404).result("Shortened URL not found");
                }
            } catch (Exception e) {
                logger.error("Error processing request", e);
                ctx.status(500).result("Internal server error: " + e.getMessage());
            }
        });

        urlShortenerApp.post("/shorten", ctx -> {
            ShortUrlRequest request = ctx.bodyAsClass(ShortUrlRequest.class);
            String longUrl = request.getLongUrl();

            if (urlValidator.isValid(longUrl)) {
                String shortUrl = urlShortenerService.shortenUrl(longUrl);
                ctx.json(new ShortUrlResponse(shortUrl));
            } else {
                String errorMessage = "Invalid URL to shorten: " + longUrl;
                logger.error(errorMessage);
                ctx.status(400).result(errorMessage);
            }
        });

    }
}
