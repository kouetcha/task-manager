package com.kouetcha.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FileTokenUtil {

    private static final Map<String, String> fileTokenMap = new ConcurrentHashMap<>();
    private static final Map<String, Instant> tokenExpirationMap = new ConcurrentHashMap<>();

    private static final Duration EXPIRATION_DURATION = Duration.ofMinutes(10); // Token valide 10 minutes

    private FileTokenUtil() {
        // Empêche l'instanciation
    }

    public static String generateToken(String fileName) {
        String token = UUID.randomUUID().toString();
        fileTokenMap.put(token, URLDecoder.decode(fileName, StandardCharsets.UTF_8));
        tokenExpirationMap.put(token, Instant.now().plus(EXPIRATION_DURATION));
        return token;
    }

    public static Optional<String> getFileNameByToken(String token) {
        Instant expiration = tokenExpirationMap.get(token);
        if (expiration == null || Instant.now().isAfter(expiration)) {
            fileTokenMap.remove(token);
            tokenExpirationMap.remove(token);
            return Optional.empty();
        }
        return Optional.ofNullable(fileTokenMap.get(token));
    }

    public static void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenExpirationMap.entrySet().removeIf(entry -> {
            boolean expired = now.isAfter(entry.getValue());
            if (expired) {
                fileTokenMap.remove(entry.getKey());
            }
            return expired;
        });
    }
}