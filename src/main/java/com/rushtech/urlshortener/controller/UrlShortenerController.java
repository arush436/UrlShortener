package com.rushtech.urlshortener.controller;

import com.rushtech.urlshortener.model.OriginalUrlResponse;
import com.rushtech.urlshortener.model.ShortUrlRequest;
import com.rushtech.urlshortener.model.ShortUrlResponse;
import com.rushtech.urlshortener.service.IUrlShortenerService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);

    private final UrlValidator urlValidator;
    private final IUrlShortenerService urlShortenerService;

    public UrlShortenerController(IUrlShortenerService urlShortenerService, UrlValidator urlValidator) {
        this.urlShortenerService = urlShortenerService;
        this.urlValidator = urlValidator;
    }

    public void startServer() {
        Javalin urlShortenerApp = Javalin.create().start(8080);

        urlShortenerApp.get("/", this::welcomeMessage);

        urlShortenerApp.get("/{token}", this::redirectToOriginalUrl);

        urlShortenerApp.get("/original/{token}", this::getOriginalUrl);

        urlShortenerApp.post("/shorten", this::shortenUrl);
    }

    private void welcomeMessage(Context ctx) {
        ctx.result("Welcome to the URL Shortener API!");
    }

    private void redirectToOriginalUrl(Context ctx) {
        String token = ctx.pathParam("token");
        String originalUrl = urlShortenerService.getOriginalUrl(token);
        if (originalUrl != null) {
            ctx.redirect(originalUrl);
        } else {
            ctx.status(404).result("Shortened URL not found");
        }
    }

    private void getOriginalUrl(Context ctx) {
        String token = ctx.pathParam("token");
        try {
            String originalUrl = urlShortenerService.getOriginalUrl(token);
            if (originalUrl != null) {
                ctx.json(new OriginalUrlResponse(originalUrl));
            } else {
                ctx.status(404).result("Shortened URL not found");
            }
        } catch (Exception e) {
            logger.error("Error processing request", e);
            ctx.status(500).result("Internal server error: " + e.getMessage());
        }
    }

    private void shortenUrl(Context ctx) {
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
    }
}
