package dev.gustavo.passin.service.auth;

import dev.gustavo.passin.controller.dto.auth.OrganizerLoginRequestDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerRegisterRequestDTO;
import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.exception.OrganizerAlreadyExistsException;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.OrganizerPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthenticationTokens register(OrganizerRegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (organizerRepository.findByEmail(email).isPresent())
            throw new OrganizerAlreadyExistsException("Organizer email is already registered");

        Organizer organizer = new Organizer();
        organizer.setName(request.name().trim());
        organizer.setEmail(email);
        organizer.setPasswordHash(passwordEncoder.encode(request.password()));
        organizer.setCreatedAt(LocalDateTime.now());
        organizerRepository.save(organizer);

        return createTokens(organizer);
    }

    public AuthenticationTokens login(OrganizerLoginRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        Organizer organizer = organizerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), organizer.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");

        return createTokens(organizer);
    }

    public AuthenticationTokens refresh(String refreshToken) {
        RefreshTokenResult refreshTokenResult = refreshTokenService.rotate(refreshToken);
        Organizer organizer = refreshTokenResult.organizer();
        String accessToken = accessTokenService.generate(new OrganizerPrincipal(organizer));
        return new AuthenticationTokens(
                accessToken,
                refreshTokenResult.refreshToken(),
                "Bearer",
                accessTokenService.getAccessTokenTtl().toSeconds());
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthenticationTokens createTokens(Organizer organizer) {
        String accessToken = accessTokenService.generate(new OrganizerPrincipal(organizer));
        String refreshToken = refreshTokenService.create(organizer);
        return new AuthenticationTokens(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenService.getAccessTokenTtl().toSeconds());
    }
}
