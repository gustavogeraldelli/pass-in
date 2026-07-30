package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.auth.AuthResponseDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerLoginRequestDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerRegisterRequestDTO;
import dev.gustavo.passin.controller.dto.auth.RefreshTokenRequestDTO;
import dev.gustavo.passin.service.auth.AuthenticationService;
import dev.gustavo.passin.service.auth.AuthenticationTokens;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid OrganizerRegisterRequestDTO request) {
        AuthenticationTokens tokens = authenticationService.register(request);
        return ResponseEntity.status(201).body(toResponse(tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid OrganizerLoginRequestDTO request) {
        AuthenticationTokens tokens = authenticationService.login(request);
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        AuthenticationTokens tokens = authenticationService.refresh(request.refreshToken());
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private AuthResponseDTO toResponse(AuthenticationTokens tokens) {
        return new AuthResponseDTO(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.tokenType(),
                tokens.expiresInSeconds());
    }
}
