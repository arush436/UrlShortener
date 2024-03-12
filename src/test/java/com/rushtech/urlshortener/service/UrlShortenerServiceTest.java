package com.rushtech.urlshortener.service;

import com.rushtech.urlshortener.dal.IUrlShortenerDAL;
import com.rushtech.urlshortener.util.ITokenGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

    private static final long EXISTING_URL_ID = 1L;
    private static final long NON_EXISTING_URL_ID = -1L;
    private static final long DEFAULT_ID = 0L;

    @Test
    public void shortenUrl_NewOriginalUrl_ShouldReturnShortenedUrl() {
        // Arrange
        String longUrl = "http://example.com";
        String token = "abc123";
        String expectedShortenedUrl = "http://localhost:8080/abc123";

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrlId(longUrl)).thenReturn(NON_EXISTING_URL_ID);

        ITokenGenerator tokenGenerator = mock(ITokenGenerator.class);
        when(tokenGenerator.generateToken()).thenReturn(token);

        UrlShortenerService urlShortenerService = new UrlShortenerService(tokenGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(longUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL).insertOriginalUrl(longUrl);
        verify(urlShortenerDAL).insertUrlMapping(token, DEFAULT_ID);
    }

    @Test
    public void shortenUrl_ExistingOriginalUrl_ShouldReturnExistingShortenedUrl() {
        // Arrange
        String existingLongUrl = "http://thisIsALongUrl.com";
        String token = "token";
        String expectedShortenedUrl = "http://localhost:8080/" + token;

        ITokenGenerator tokenGenerator = mock(ITokenGenerator.class);

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrlId(existingLongUrl)).thenReturn(EXISTING_URL_ID);
        when(urlShortenerDAL.getTokenForOriginalUrl(EXISTING_URL_ID)).thenReturn(token);

        UrlShortenerService urlShortenerService = new UrlShortenerService(tokenGenerator, urlShortenerDAL);

        // Act
        String shortenedUrl = urlShortenerService.shortenUrl(existingLongUrl);

        // Assert
        assertEquals(expectedShortenedUrl, shortenedUrl);
        verify(urlShortenerDAL, never()).insertOriginalUrl(existingLongUrl);
        verify(urlShortenerDAL, never()).updateToken(token, EXISTING_URL_ID);
    }


    @Test
    public void getOriginalUrl_ShouldReturnOriginalUrl() {
        // Arrange
        String token = "abc123";
        String originalUrl = "http://example.com";

        ITokenGenerator tokenGenerator = mock(ITokenGenerator.class);
        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.getOriginalUrl(token)).thenReturn(originalUrl);

        UrlShortenerService urlShortenerService = new UrlShortenerService(tokenGenerator, urlShortenerDAL);

        // Act
        String retrievedOriginalUrl = urlShortenerService.getOriginalUrl(token);

        // Assert
        assertEquals(originalUrl, retrievedOriginalUrl);
    }

    @Test
    public void deleteShortUrl_ValidToken_ShouldReturnTrue() {
        // Arrange
        String testToken = "testToken";

        IUrlShortenerDAL urlShortenerDAL = mock(IUrlShortenerDAL.class);
        when(urlShortenerDAL.deleteShortUrl(testToken)).thenReturn(true);

        ITokenGenerator tokenGenerator = mock(ITokenGenerator.class);

        UrlShortenerService urlShortenerService = new UrlShortenerService(tokenGenerator, urlShortenerDAL);

        // Act
        boolean result = urlShortenerService.deleteShortUrl(testToken);

        // Assert
        assertTrue(result);
    }
}
