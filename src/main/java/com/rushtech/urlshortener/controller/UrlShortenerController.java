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
        setupRoutes(urlShortenerApp);
    }

    private void setupRoutes(Javalin app) {
        setupWelcomeRoute(app);
        setupRedirectRoute(app);
        setupOriginalUrlRoute(app);
        setupShortenUrlRoute(app);
        setupDeleteShortUrlRoute(app);
    }

    private void setupWelcomeRoute(Javalin app) {
        app.get("/", this::welcomeMessage);
    }

    private void setupRedirectRoute(Javalin app) {
        app.get("/{token}", this::redirectToOriginalUrl);
    }

    private void setupOriginalUrlRoute(Javalin app) {
        app.get("/original/{token}", this::getOriginalUrl);
    }

    private void setupShortenUrlRoute(Javalin app) {
        app.post("/shorten", this::shortenUrl);
    }

    private void setupDeleteShortUrlRoute(Javalin app) {
        app.delete("/short/{token}", this::deleteShortUrl);
    }

    private void welcomeMessage(Context ctx) {
        ctx.result("Welcome to the URL Shortener API!");
    }

    private void redirectToOriginalUrl(Context ctx) {
        String token = ctx.pathParam("token");
        String originalUrl = urlShortenerService.getOriginalUrl(token);
        if (originalUrl != null) {
            urlShortenerService.incrementRedirectCount(originalUrl);
            ctx.redirect(originalUrl);
        } else {
            logger.error("Shortened URL not found");
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

    private void deleteShortUrl(Context ctx) {
        String token = ctx.pathParam("token");
        boolean deleted = urlShortenerService.deleteShortUrl(token);
        if (deleted) {
            ctx.result("Short URL and associated long URL deleted successfully");
        } else {
            logger.error("Short URL not found");
            ctx.status(404).result("Short URL not found");
        }
    }
}
