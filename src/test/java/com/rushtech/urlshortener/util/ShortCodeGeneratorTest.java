package com.rushtech.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShortCodeGeneratorTest {

    @Test
    public void generateShortCode_ValidInput_ReturnsSixCharacters() {
        ShortCodeGenerator shortCodeGenerator = new ShortCodeGenerator();
        String shortCode = shortCodeGenerator.generateShortCode();

        assertEquals(6, shortCode.length(), "Generated short code length should be 6");
    }

    @Test
    public void generateShortCode_ValidInput_ReturnsHexadecimalCharacters() {
        ShortCodeGenerator shortCodeGenerator = new ShortCodeGenerator();
        String shortCode = shortCodeGenerator.generateShortCode();

        assertTrue(shortCode.matches("[a-fA-F0-9]{6}"), "Generated short code should consist of hexadecimal characters");
    }

    @Test
    public void generateShortCode_CalledTwice_ReturnsDifferentCodes() {
        ShortCodeGenerator shortCodeGenerator = new ShortCodeGenerator();
        String code1 = shortCodeGenerator.generateShortCode();
        String code2 = shortCodeGenerator.generateShortCode();

        assertNotEquals(code1, code2, "Generated short codes should be different");
    }
}
