package com.rushtech.urlshortener.util;

import java.util.UUID;

public class TokenGenerator implements ITokenGenerator {

    private static final int CODE_LENGTH = 6;

    public String generateToken() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid.substring(0, CODE_LENGTH);
    }
}
