package com.marketplace.infrastructure.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Check and increment a rate-limit counter.
     * Returns true if the request is allowed, false if the limit has been exceeded.
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            boolean allowed = count != null && count <= maxRequests;
            if (!allowed) {
                log.debug("Rate limit exceeded for key={}, count={}, max={}", key, count, maxRequests);
            }
            return allowed;
        } catch (Exception e) {
            // If Redis is down, allow the request to avoid blocking users
            log.warn("Rate limit check failed (Redis error), allowing request: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Enforce the rate limit; throw RateLimitExceededException if exceeded.
     */
    public void enforce(String key, int maxRequests, int windowSeconds) {
        if (!isAllowed(key, maxRequests, windowSeconds)) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }
}
