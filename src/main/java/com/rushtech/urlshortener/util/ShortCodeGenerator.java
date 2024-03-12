package com.rushtech.urlshortener.util;

import java.util.UUID;

public class ShortCodeGenerator {

    private static final int CODE_LENGTH = 6;

    public static String generateShortCode() {
        // Generate a UUID
        String uuid = UUID.randomUUID().toString();

        // Remove hyphens from the UUID
        uuid = uuid.replaceAll("-", "");

        // Extract the first CODE_LENGTH characters from the UUID
        return uuid.substring(0, CODE_LENGTH);
    }
}
