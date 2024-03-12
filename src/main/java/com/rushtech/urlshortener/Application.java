package com.rushtech.urlshortener;

import com.github.benmanes.caffeine.cache.Cache;
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

        IUrlShortenerDAL urlShortenerDAL = new UrlShortenerDAL(properties.getProperty("database.url"));
        ITokenGenerator tokenGenerator = new TokenGenerator();

        long expireAfterWrite = Long.parseLong(properties.getProperty("cache.expireAfterWriteMinutes"));
        long maximumSize = Long.parseLong(properties.getProperty("cache.maximumSize"));
        CacheManager.configureCache(expireAfterWrite, maximumSize);

        IUrlShortenerService urlShortenerService = new UrlShortenerService(tokenGenerator, urlShortenerDAL, CacheManager.getOriginalUrlCache());

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
}
