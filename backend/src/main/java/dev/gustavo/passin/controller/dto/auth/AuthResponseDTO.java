package dev.gustavo.passin.controller.dto.auth;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Long expiresInSeconds) {
}
