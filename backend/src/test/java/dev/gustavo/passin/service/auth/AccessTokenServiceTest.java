package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.security.OrganizerPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTokenServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-30T12:00:00Z"), ZoneOffset.UTC);
    private final AccessTokenService accessTokenService = new AccessTokenService(
            clock,
            "test-secret",
            Duration.ofMinutes(15));

    @Test
    void shouldGenerateAndValidateAccessToken() {
        String token = accessTokenService.generate(new OrganizerPrincipal(organizer()));

        String subject = accessTokenService.getSubject(token);

        assertThat(subject).isEqualTo("organizer-1");
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThatThrownBy(() -> accessTokenService.getSubject("invalid-token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid access token");
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
