package dev.gustavo.passin.service;

import dev.gustavo.passin.controller.dto.auth.AuthResponseDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerLoginRequestDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerRegisterRequestDTO;
import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.exception.OrganizerAlreadyExistsException;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.JwtService;
import dev.gustavo.passin.security.OrganizerPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDTO register(OrganizerRegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (organizerRepository.findByEmail(email).isPresent())
            throw new OrganizerAlreadyExistsException("Organizer email is already registered");

        Organizer organizer = new Organizer();
        organizer.setName(request.name().trim());
        organizer.setEmail(email);
        organizer.setPasswordHash(passwordEncoder.encode(request.password()));
        organizer.setCreatedAt(LocalDateTime.now());
        organizerRepository.save(organizer);

        String accessToken = jwtService.generateAccessToken(new OrganizerPrincipal(organizer));
        return new AuthResponseDTO(accessToken, "Bearer", jwtService.getAccessTokenTtl().toSeconds());
    }

    public AuthResponseDTO login(OrganizerLoginRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        Organizer organizer = organizerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), organizer.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");

        String accessToken = jwtService.generateAccessToken(new OrganizerPrincipal(organizer));
        return new AuthResponseDTO(accessToken, "Bearer", jwtService.getAccessTokenTtl().toSeconds());
    }
}
