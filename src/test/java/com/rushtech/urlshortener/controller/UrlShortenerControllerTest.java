package com.rushtech.urlshortener.controller;

import com.rushtech.urlshortener.service.IUrlShortenerService;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class UrlShortenerControllerTest {

    private UrlShortenerController urlShortenerController;
    private UrlValidator urlValidatorMock;
    private IUrlShortenerService urlShortenerServiceMock;

    @BeforeEach
    public void setUp() {
        urlValidatorMock = mock(UrlValidator.class);
        urlShortenerServiceMock = mock(IUrlShortenerService.class);
        urlShortenerController = new UrlShortenerController(urlShortenerServiceMock, urlValidatorMock);
    }

    @Test
    public void testStartServer() {
    }
}
