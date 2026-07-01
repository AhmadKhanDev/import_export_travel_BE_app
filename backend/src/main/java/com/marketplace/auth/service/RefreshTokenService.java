package com.marketplace.auth.service;

import com.marketplace.auth.entity.RefreshToken;
import com.marketplace.auth.repository.RefreshTokenRepository;
import com.marketplace.common.exception.UnauthorizedException;
import com.marketplace.common.util.TokenHashUtil;
import com.marketplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = TokenHashUtil.generateSecureToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        refreshTokenRepository.revokeByTokenHash(tokenHash);
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
