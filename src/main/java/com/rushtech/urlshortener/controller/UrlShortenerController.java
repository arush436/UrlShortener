package com.rushtech.urlshortener.controller;

import com.rushtech.urlshortener.dal.UrlShortenerDAL;
import com.rushtech.urlshortener.model.OriginalUrlResponse;
import com.rushtech.urlshortener.service.UrlShortenerService;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerDAL.class);

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    public void startServer() {
        Javalin app = Javalin.create().start(8080);

        app.get("/original/{shortCode}", ctx -> {
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
                ctx.status(500).result("Internal server error");
                e.printStackTrace();
            }
        });
    }
}
