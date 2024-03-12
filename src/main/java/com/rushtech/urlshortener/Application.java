package com.rushtech.urlshortener;

import com.rushtech.urlshortener.controller.UrlShortenerController;
import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.dal.UrlShortenerDAL;
import com.rushtech.urlshortener.service.IUrlShortenerService;
import com.rushtech.urlshortener.service.UrlShortenerService;
import com.rushtech.urlshortener.util.IShortCodeGenerator;
import com.rushtech.urlshortener.util.ShortCodeGenerator;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        Properties properties = loadConfiguration();

        IUrlShortenerDAL urlShortenerDAL = new UrlShortenerDAL(properties.getProperty("database.url"));
        IShortCodeGenerator shortCodeGenerator = new ShortCodeGenerator();
        IUrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

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
