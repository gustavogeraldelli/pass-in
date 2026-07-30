package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.entity.RefreshToken;
import dev.gustavo.passin.exception.InvalidRefreshTokenException;
import dev.gustavo.passin.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.refresh-token-ttl}")
    private Duration refreshTokenTtl;

    @Transactional
    public String create(Organizer organizer) {
        String rawRefreshToken = generateRefreshToken();
        Instant now = Instant.now(clock);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setOrganizer(organizer);
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(now.plus(refreshTokenTtl));
        refreshToken.setCreatedAt(now);

        refreshTokenRepository.save(refreshToken);
        return rawRefreshToken;
    }

    @Transactional
    public RefreshTokenResult rotate(String rawRefreshToken) {
        RefreshToken currentToken = findValidToken(rawRefreshToken);
        currentToken.setRevokedAt(Instant.now(clock));
        String nextToken = create(currentToken.getOrganizer());
        return new RefreshTokenResult(currentToken.getOrganizer(), nextToken);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        RefreshToken refreshToken = findValidToken(rawRefreshToken);
        refreshToken.setRevokedAt(Instant.now(clock));
    }

    String hash(String rawRefreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = digest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedToken);
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private RefreshToken findValidToken(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null || Instant.now(clock).isAfter(refreshToken.getExpiresAt()))
            throw new InvalidRefreshTokenException("Invalid refresh token");

        return refreshToken;
    }

    private String generateRefreshToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
