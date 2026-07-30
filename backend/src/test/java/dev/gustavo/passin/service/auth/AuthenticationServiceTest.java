package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.controller.dto.auth.OrganizerLoginRequestDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerRegisterRequestDTO;
import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.exception.OrganizerAlreadyExistsException;
import dev.gustavo.passin.repository.OrganizerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRegisterOrganizerAndReturnTokens() {
        when(organizerRepository.findByEmail("gus@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(organizerRepository.save(any(Organizer.class))).thenAnswer(invocation -> {
            Organizer organizer = invocation.getArgument(0);
            organizer.setId("organizer-1");
            return organizer;
        });
        when(accessTokenService.generate(any())).thenReturn("access-token");
        when(accessTokenService.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(refreshTokenService.create(any())).thenReturn("refresh-token");

        var response = authenticationService.register(new OrganizerRegisterRequestDTO(
                "Gustavo",
                " GUS@EXAMPLE.COM ",
                "password123"));

        ArgumentCaptor<Organizer> organizerCaptor = ArgumentCaptor.forClass(Organizer.class);
        verify(organizerRepository).save(organizerCaptor.capture());
        Organizer organizer = organizerCaptor.getValue();

        assertThat(organizer.getName()).isEqualTo("Gustavo");
        assertThat(organizer.getEmail()).isEqualTo("gus@example.com");
        assertThat(organizer.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(organizer.getCreatedAt()).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void shouldThrowWhenRegisteringExistingEmail() {
        when(organizerRepository.findByEmail("gus@example.com")).thenReturn(Optional.of(organizer()));

        assertThatThrownBy(() -> authenticationService.register(new OrganizerRegisterRequestDTO(
                "Gustavo",
                "gus@example.com",
                "password123")))
                .isInstanceOf(OrganizerAlreadyExistsException.class)
                .hasMessage("Organizer email is already registered");
    }

    @Test
    void shouldLoginOrganizerAndReturnTokens() {
        Organizer organizer = organizer();
        when(organizerRepository.findByEmail("gus@example.com")).thenReturn(Optional.of(organizer));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(accessTokenService.generate(any())).thenReturn("access-token");
        when(accessTokenService.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(refreshTokenService.create(organizer)).thenReturn("refresh-token");

        var response = authenticationService.login(new OrganizerLoginRequestDTO("gus@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void shouldRefreshTokens() {
        Organizer organizer = organizer();
        when(refreshTokenService.rotate("refresh-token")).thenReturn(new RefreshTokenResult(organizer, "new-refresh-token"));
        when(accessTokenService.generate(any())).thenReturn("new-access-token");
        when(accessTokenService.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        var response = authenticationService.refresh("refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void shouldLogout() {
        authenticationService.logout("refresh-token");

        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void shouldThrowWhenPasswordIsInvalid() {
        Organizer organizer = organizer();
        when(organizerRepository.findByEmail("gus@example.com")).thenReturn(Optional.of(organizer));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new OrganizerLoginRequestDTO("gus@example.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
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
