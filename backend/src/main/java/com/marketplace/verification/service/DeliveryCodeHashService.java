package com.marketplace.verification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class DeliveryCodeHashService {

    private final String secret;

    public DeliveryCodeHashService(
            @Value("${app.delivery-code.secret:dev-delivery-code-secret-change-me}") String secret) {
        this.secret = secret;
    }

    public String hashCode(UUID bookingId, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = bookingId + ":" + code.trim() + ":" + secret;
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public boolean matches(UUID bookingId, String rawCode, String storedHash) {
        return hashCode(bookingId, rawCode).equals(storedHash);
    }
}
