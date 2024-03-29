package com.rushtech.urlshortener;

import com.rushtech.urlshortener.controller.UrlShortenerController;
import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.dal.UrlShortenerDAL;
import com.rushtech.urlshortener.service.IUrlShortenerService;
import com.rushtech.urlshortener.service.UrlShortenerService;
import com.rushtech.urlshortener.util.CacheManager;
import com.rushtech.urlshortener.util.ITokenGenerator;
import com.rushtech.urlshortener.util.TokenGenerator;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        Properties properties = loadConfiguration();
        configureCache(properties);
        IUrlShortenerService urlShortenerService = createUrlShortenerService(properties);
        UrlValidator urlValidator = new UrlValidator();
        UrlShortenerController urlShortenerController = new UrlShortenerController(urlShortenerService, urlValidator);

        urlShortenerController.startServer();
    }

    private static Properties loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = Application.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    private static void configureCache(Properties properties) {
        long expireAfterWrite = Long.parseLong(properties.getProperty("cache.expireAfterWriteMinutes"));
        long maximumSize = Long.parseLong(properties.getProperty("cache.maximumSize"));
        CacheManager.configureCache(expireAfterWrite, maximumSize);
    }

    private static IUrlShortenerService createUrlShortenerService(Properties properties) {
        IUrlShortenerDAL urlShortenerDAL = createUrlShortenerDAL(properties);
        ITokenGenerator tokenGenerator = new TokenGenerator();
        return new UrlShortenerService(tokenGenerator, urlShortenerDAL, CacheManager.getOriginalUrlCache());
    }

    private static IUrlShortenerDAL createUrlShortenerDAL(Properties properties) {
        return new UrlShortenerDAL(
                properties.getProperty("database.url"),
                Integer.parseInt(properties.getProperty("database.maxPoolSize")),
                Integer.parseInt(properties.getProperty("database.connectionTimeoutMilliseconds")),
                Integer.parseInt(properties.getProperty("url.expiryDateMonthsInFuture"))
        );
    }
}
