package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.auth.AuthResponseDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerLoginRequestDTO;
import dev.gustavo.passin.controller.dto.auth.OrganizerRegisterRequestDTO;
import dev.gustavo.passin.service.AuthService;
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

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid OrganizerRegisterRequestDTO request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid OrganizerLoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
