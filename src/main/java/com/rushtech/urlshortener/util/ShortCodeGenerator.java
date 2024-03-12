package com.rushtech.urlshortener.util;

import java.util.UUID;

public class ShortCodeGenerator implements IShortCodeGenerator {

    private static final int CODE_LENGTH = 6;

    public String generateShortCode() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid.substring(0, CODE_LENGTH);
    }
}
