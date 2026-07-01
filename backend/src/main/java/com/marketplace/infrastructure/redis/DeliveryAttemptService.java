package com.marketplace.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.delivery-code.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.delivery-code.attempt-window-minutes:10}")
    private int windowMinutes;

    /**
     * Increment the wrong-attempt counter for a booking+user combination.
     * Returns the new count.
     */
    public long incrementAttempts(UUID bookingId, UUID userId) {
        String key = CacheKeyUtil.deliveryAttempts(bookingId, userId);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // Set expiry only on first attempt
            redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
        }
        return count != null ? count : 1;
    }

    /**
     * Check whether the maximum wrong-attempt threshold has been reached.
     */
    public boolean isExceeded(UUID bookingId, UUID userId) {
        String key = CacheKeyUtil.deliveryAttempts(bookingId, userId);
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            return false;
        }
        long count = Long.parseLong(val.toString());
        return count >= maxAttempts;
    }

    /**
     * Reset the attempt counter after a successful delivery verification.
     */
    public void resetAttempts(UUID bookingId, UUID userId) {
        String key = CacheKeyUtil.deliveryAttempts(bookingId, userId);
        redisTemplate.delete(key);
    }
}
