package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.auth.AuthResponseDTO;
import dev.gustavo.passin.exception.OrganizerAlreadyExistsException;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.JwtService;
import dev.gustavo.passin.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OrganizerRepository organizerRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldRegisterOrganizer() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponseDTO("access-token", "Bearer", 900L));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Gustavo",
                                  "email": "gus@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fields[*].field", hasItem("name")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("email")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("password")));
    }

    @Test
    void shouldReturnConflictWhenRegisteringExistingOrganizer() throws Exception {
        when(authService.register(any()))
                .thenThrow(new OrganizerAlreadyExistsException("Organizer email is already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Gustavo",
                                  "email": "gus@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Organizer email is already registered"));
    }

    @Test
    void shouldLoginOrganizer() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponseDTO("access-token", "Bearer", 900L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "gus@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "gus@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
