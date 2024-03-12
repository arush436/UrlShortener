package com.rushtech.urlshortener.util;

import java.security.SecureRandom;

public class TokenGenerator implements ITokenGenerator {

    private static final int CODE_LENGTH = 6;
    private static final int RANDOM_INTEGER_RANGE = 16;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateToken() {
        StringBuilder token = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomValue = secureRandom.nextInt(RANDOM_INTEGER_RANGE); // Generate a random integer between 0 and RANDOM_INTEGER_RANGE (inclusive)
            token.append(Integer.toHexString(randomValue)); // Convert the integer to a hexadecimal character
        }
        return token.toString();
    }
}
