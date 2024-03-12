package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.IShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

    @Test
    public void shortenUrl_NewOriginalUrl_ShouldReturnShortenedUrl() {
        // Arrange
        String longUrl = "http://example.com";
        String shortCode = "abc123";
        String expectedShortenedUrl = "http://localhost:8080/abc123";

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrlId(longUrl)).thenReturn(-1L);

        IShortCodeGenerator shortCodeGenerator = mock(IShortCodeGenerator.class);
        when(shortCodeGenerator.generateShortCode()).thenReturn(shortCode);

        UrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(longUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL).insertOriginalUrl(longUrl);
        verify(urlShortenerDAL).insertUrlMapping(shortCode, 0L);
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
        when(urlShortenerDAL.getOriginalUrlId(longUrl)).thenReturn(1L); // Request URL exists
        doNothing().when(urlShortenerDAL).updateShortCode(newShortCode, 1L);

        UrlShortenerService urlShortenerService = new UrlShortenerService(shortCodeGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(longUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL, never()).insertOriginalUrl(longUrl);
        verify(urlShortenerDAL).updateShortCode(newShortCode, 1L);
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
