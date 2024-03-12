package com.rushtech.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenGeneratorTest {

    @Test
    public void generateToken_ValidInput_ReturnsSixCharacters() {
        int expectedTokenGeneratorLength = 6;

        TokenGenerator tokenGenerator = new TokenGenerator();
        String token = tokenGenerator.generateToken();

        assertEquals(expectedTokenGeneratorLength, token.length(), "Generated token length should be 6");
    }

    @Test
    public void generateToken_ValidInput_ReturnsHexadecimalCharacters() {
        TokenGenerator tokenGenerator = new TokenGenerator();
        String token = tokenGenerator.generateToken();

        assertTrue(token.matches("[a-fA-F0-9]{6}"), "Generated token should consist of hexadecimal characters");
    }

    @Test
    public void generateToken_CalledTwice_ReturnsDifferentCodes() {
        TokenGenerator tokenGenerator = new TokenGenerator();
        String token1 = tokenGenerator.generateToken();
        String token2 = tokenGenerator.generateToken();

        assertNotEquals(token1, token2, "Generated tokens should be different");
    }
}
