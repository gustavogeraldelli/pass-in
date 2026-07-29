package dev.gustavo.passin.service;

import dev.gustavo.passin.exception.InvalidCheckInTokenException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckInTokenServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-07-29T10:00:00Z"), ZoneOffset.UTC);
    private final CheckInTokenService tokenService = new CheckInTokenService("test-secret", Duration.ofHours(1), fixedClock);

    @Test
    void shouldGenerateSignedTokenAndReturnAttendeeId() {
        String token = tokenService.generateToken("attendee-1");

        String attendeeId = tokenService.getAttendeeId(token);

        assertThat(attendeeId).isEqualTo("attendee-1");
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = tokenService.generateToken("attendee-1");
        String tamperedToken = token.replace("a", "b");

        assertThatThrownBy(() -> tokenService.getAttendeeId(tamperedToken))
                .isInstanceOf(InvalidCheckInTokenException.class)
                .hasMessage("Invalid check-in token");
    }

    @Test
    void shouldRejectExpiredToken() {
        CheckInTokenService expiredTokenService = new CheckInTokenService("test-secret", Duration.ofSeconds(-1), fixedClock);
        String token = expiredTokenService.generateToken("attendee-1");

        assertThatThrownBy(() -> tokenService.getAttendeeId(token))
                .isInstanceOf(InvalidCheckInTokenException.class)
                .hasMessage("Expired check-in token");
    }
}
