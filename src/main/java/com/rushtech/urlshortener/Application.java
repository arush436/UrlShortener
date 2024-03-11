package com.rushtech.urlshortener;

import com.rushtech.urlshortener.controller.UrlShortenerController;
import com.rushtech.urlshortener.dal.UrlShortenerDAL;
import com.rushtech.urlshortener.service.UrlShortenerService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        Properties properties = loadConfiguration();

        UrlShortenerDAL urlShortenerDAL = new UrlShortenerDAL(properties.getProperty("database.url"));
        UrlShortenerService urlShortenerService = new UrlShortenerService(urlShortenerDAL);
        UrlShortenerController urlShortenerController = new UrlShortenerController(urlShortenerService);

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
