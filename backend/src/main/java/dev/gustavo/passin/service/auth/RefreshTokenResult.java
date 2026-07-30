package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.entity.Organizer;

public record RefreshTokenResult(
        Organizer organizer,
        String refreshToken) {
}
