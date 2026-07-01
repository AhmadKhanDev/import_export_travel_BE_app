package com.marketplace.infrastructure.redis;

import java.util.UUID;

public final class CacheKeyUtil {

    private CacheKeyUtil() {}

    // JWT blacklist key
    public static String jwtBlacklist(String tokenHash) {
        return "auth:blacklist:" + tokenHash;
    }

    // Rate limiting keys
    public static String rateLimitLogin(String ip, String email) {
        return "rate:login:" + ip + ":" + email;
    }

    public static String rateLimitRegister(String ip) {
        return "rate:register:" + ip;
    }

    public static String rateLimitDeliveryGenerate(UUID bookingId, UUID userId) {
        return "rate:delivery:generate:" + bookingId + ":" + userId;
    }

    public static String rateLimitDeliveryVerify(UUID bookingId, UUID userId) {
        return "rate:delivery:verify:" + bookingId + ":" + userId;
    }

    public static String rateLimitGeneral(String identifier) {
        return "rate:general:" + identifier;
    }

    // Delivery code wrong-attempt counter
    public static String deliveryAttempts(UUID bookingId, UUID userId) {
        return "delivery:attempts:" + bookingId + ":" + userId;
    }

    // Cache keys
    public static String ratingSummary(UUID userId) {
        return "cache:ratingSummary:" + userId;
    }

    public static String dashboardSummary() {
        return "cache:dashboardSummary";
    }
}
