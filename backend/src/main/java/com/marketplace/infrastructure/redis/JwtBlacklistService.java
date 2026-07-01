package com.marketplace.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Blacklist an access token by storing its SHA-256 hash in Redis.
     * TTL is set to remaining token lifetime so the key auto-expires.
     */
    public void blacklist(String token, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        try {
            String hash = sha256(token);
            String key = CacheKeyUtil.jwtBlacklist(hash);
            redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttlMillis));
            log.debug("JWT blacklisted, TTL={}ms", ttlMillis);
        } catch (Exception e) {
            log.warn("Failed to blacklist JWT token: {}", e.getMessage());
        }
    }

    /**
     * Check if the given access token has been blacklisted.
     */
    public boolean isBlacklisted(String token) {
        try {
            String hash = sha256(token);
            String key = CacheKeyUtil.jwtBlacklist(hash);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check JWT blacklist, allowing request: {}", e.getMessage());
            return false;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
