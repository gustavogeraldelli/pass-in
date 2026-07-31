package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.event.EventDetailsResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventResponseItemDTO;
import dev.gustavo.passin.config.SecurityConfig;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.ApiAuthenticationEntryPoint;
import dev.gustavo.passin.security.JwtAuthenticationFilter;
import dev.gustavo.passin.service.auth.AccessTokenService;
import dev.gustavo.passin.service.AttendeeService;
import dev.gustavo.passin.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, ApiAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class})
class EventSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private AttendeeService attendeeService;

    @MockitoBean
    private OrganizerRepository organizerRepository;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    void shouldRejectProtectedEventListWithoutToken() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void shouldRejectAttendeeExportWithoutToken() throws Exception {
        mockMvc.perform(get("/events/event-1/attendees/export"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void shouldAllowPublicEventDetailsWithoutToken() throws Exception {
        EventResponseItemDTO event = new EventResponseItemDTO(
                "event-1",
                "Java Conf",
                "Backend event",
                "java-conf",
                100,
                3,
                2);
        when(eventService.getEvent("event-1")).thenReturn(new EventDetailsResponseDTO(event));

        mockMvc.perform(get("/events/event-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.id").value("event-1"));
    }

}
