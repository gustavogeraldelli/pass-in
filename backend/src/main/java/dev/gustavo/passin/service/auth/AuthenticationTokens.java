package dev.gustavo.passin.service.auth;

public record AuthenticationTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresInSeconds) {
}
