package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.entity.RefreshToken;
import dev.gustavo.passin.exception.InvalidRefreshTokenException;
import dev.gustavo.passin.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-30T12:00:00Z"), ZoneOffset.UTC);
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, clock);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTtl", Duration.ofDays(7));
    }

    @Test
    void shouldCreateRefreshTokenAndStoreOnlyHash() {
        Organizer organizer = organizer();

        String rawRefreshToken = refreshTokenService.create(organizer);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken refreshToken = tokenCaptor.getValue();

        assertThat(rawRefreshToken).isNotBlank();
        assertThat(refreshToken.getOrganizer()).isEqualTo(organizer);
        assertThat(refreshToken.getTokenHash()).isNotEqualTo(rawRefreshToken);
        assertThat(refreshToken.getTokenHash()).hasSize(64);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(Instant.parse("2026-08-06T12:00:00Z"));
        assertThat(refreshToken.getCreatedAt()).isEqualTo(Instant.parse("2026-07-30T12:00:00Z"));
    }

    @Test
    void shouldRotateRefreshToken() {
        RefreshToken currentToken = validRefreshToken();
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash("refresh-token")))
                .thenReturn(Optional.of(currentToken));

        RefreshTokenResult result = refreshTokenService.rotate("refresh-token");

        assertThat(currentToken.getRevokedAt()).isEqualTo(Instant.parse("2026-07-30T12:00:00Z"));
        assertThat(result.organizer()).isEqualTo(currentToken.getOrganizer());
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void shouldRevokeRefreshToken() {
        RefreshToken currentToken = validRefreshToken();
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash("refresh-token")))
                .thenReturn(Optional.of(currentToken));

        refreshTokenService.revoke("refresh-token");

        assertThat(currentToken.getRevokedAt()).isEqualTo(Instant.parse("2026-07-30T12:00:00Z"));
    }

    @Test
    void shouldRejectReusedRefreshToken() {
        RefreshToken currentToken = validRefreshToken();
        currentToken.setRevokedAt(Instant.parse("2026-07-30T11:00:00Z"));
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash("refresh-token")))
                .thenReturn(Optional.of(currentToken));

        assertThatThrownBy(() -> refreshTokenService.rotate("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken currentToken = validRefreshToken();
        currentToken.setExpiresAt(Instant.parse("2026-07-30T11:00:00Z"));
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash("refresh-token")))
                .thenReturn(Optional.of(currentToken));

        assertThatThrownBy(() -> refreshTokenService.rotate("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    private RefreshToken validRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setOrganizer(organizer());
        refreshToken.setTokenHash(refreshTokenService.hash("refresh-token"));
        refreshToken.setExpiresAt(Instant.parse("2026-08-06T12:00:00Z"));
        refreshToken.setCreatedAt(Instant.parse("2026-07-30T12:00:00Z"));
        return refreshToken;
    }

    private Organizer organizer() {
        Organizer organizer = new Organizer();
        organizer.setId("organizer-1");
        organizer.setName("Gustavo");
        organizer.setEmail("gus@example.com");
        organizer.setPasswordHash("hashed-password");
        return organizer;
    }
}
