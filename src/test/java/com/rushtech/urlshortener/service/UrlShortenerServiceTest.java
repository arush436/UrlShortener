package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.IShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

    private static final long EXISTING_URL_ID = 1L;
    private static final long NON_EXISTING_URL_ID = -1L;
    private static final long DEFAULT_ID = 0L;

    @Test
    public void shortenUrl_NewOriginalUrl_ShouldReturnShortenedUrl() {
        // Arrange
        String longUrl = "http://example.com";
        String shortCode = "abc123";
        String expectedShortenedUrl = "http://localhost:8080/abc123";

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrlId(longUrl)).thenReturn(NON_EXISTING_URL_ID);

        IShortCodeGenerator shortCodeGenerator = mock(IShortCodeGenerator.class);
        when(shortCodeGenerator.generateShortCode()).thenReturn(shortCode);

        UrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(longUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL).insertOriginalUrl(longUrl);
        verify(urlShortenerDAL).insertUrlMapping(shortCode, DEFAULT_ID);
    }

    @Test
    public void shortenUrl_ExistingOriginalUrl_ShouldReturnShortenedUrl() {
        // Arrange
        String longUrl = "http://example.com";
        String newShortCode = "newShortCode";
        String expectedShortenedUrl = "http://localhost:8080/newShortCode";

        IShortCodeGenerator shortCodeGenerator = mock(IShortCodeGenerator.class);
        when(shortCodeGenerator.generateShortCode()).thenReturn(newShortCode);

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrlId(longUrl)).thenReturn(EXISTING_URL_ID);
        doNothing().when(urlShortenerDAL).updateShortCode(newShortCode, EXISTING_URL_ID);

        UrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(longUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL, never()).insertOriginalUrl(longUrl);
        verify(urlShortenerDAL).updateShortCode(newShortCode, EXISTING_URL_ID);
    }


    @Test
    public void getOriginalUrl_ShouldReturnOriginalUrl() {
        // Arrange
        String shortCode = "abc123";
        String originalUrl = "http://example.com";

        IShortCodeGenerator shortCodeGenerator = mock(IShortCodeGenerator.class);
        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrl(shortCode)).thenReturn(originalUrl);

        UrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

        // Act
        String retrievedOriginalUrl = urlShortenerService.getOriginalUrl(shortCode);

        // Assert
        assertEquals(originalUrl, retrievedOriginalUrl);
    }
}
