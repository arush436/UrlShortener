package com.rushtech.urlshortener.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private static Cache<String, String> originalUrlCache;

    public static void configureCache(long expireAfterWriteMinutes, long maximumSize) {
        originalUrlCache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .build();
    }

    public static Cache<String, String> getOriginalUrlCache() {
        return originalUrlCache;
    }
}
