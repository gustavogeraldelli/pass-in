package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventDetailsResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventResponseItemDTO;
import dev.gustavo.passin.config.SecurityConfig;
import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.exception.AttendeeAlreadyExistsException;
import dev.gustavo.passin.exception.EventNotFoundException;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.ApiAuthenticationEntryPoint;
import dev.gustavo.passin.security.OrganizerPrincipal;
import dev.gustavo.passin.service.auth.AccessTokenService;
import dev.gustavo.passin.service.AttendeeService;
import dev.gustavo.passin.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, ApiAuthenticationEntryPoint.class})
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class})
class EventControllerTest {

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
    void shouldReturnEvents() throws Exception {
        when(eventService.getEventsByOrganizer("organizer-1")).thenReturn(new EventListResponseDTO(List.of()));

        mockMvc.perform(get("/events").with(authentication(authenticationToken(organizerPrincipal()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray());

        verify(eventService).getEventsByOrganizer("organizer-1");
    }

    @Test
    void shouldCreateEvent() throws Exception {
        OrganizerPrincipal organizer = organizerPrincipal();
        when(eventService.createEventForOrganizer(any(), any())).thenReturn("event-1");

        mockMvc.perform(post("/events")
                        .with(authentication(authenticationToken(organizer)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Java Conf",
                                  "details": "Backend event",
                                  "maximumAttendees": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/events/event-1"));

        verify(eventService).createEventForOrganizer(any(), eq(organizer.getOrganizer()));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingEventWithInvalidPayload() throws Exception {
        mockMvc.perform(post("/events")
                        .with(authentication(authenticationToken(organizerPrincipal())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "details": "",
                                  "maximumAttendees": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fields[*].field", hasItem("title")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("details")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("maximumAttendees")))
                .andExpect(jsonPath("$.fields[*].message", hasItem("Title is required")))
                .andExpect(jsonPath("$.fields[*].message", hasItem("Details are required")))
                .andExpect(jsonPath("$.fields[*].message", hasItem("Maximum attendees must be greater than zero")));
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        when(eventService.getEvent("missing-event"))
                .thenThrow(new EventNotFoundException("Event with id missing-event was not found"));

        mockMvc.perform(get("/events/missing-event"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event with id missing-event was not found"))
                .andExpect(jsonPath("$.path").value("/events/missing-event"));
    }

    @Test
    void shouldReturnEventAttendeesWithPagination() throws Exception {
        when(attendeeService.getEventsAttendee(eq("event-1"), eq("ana"), any()))
                .thenReturn(new AttendeeListResponseDTO(List.of(), 0, 100, 0L, 0));

        mockMvc.perform(get("/events/event-1/attendees")
                        .with(authentication(authenticationToken(organizerPrincipal())))
                        .param("page", "-1")
                        .param("size", "200")
                        .param("query", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        var pageableCaptor = forClass(Pageable.class);

        verify(eventService).getEventByIdAndOrganizer("event-1", "organizer-1");
        verify(attendeeService).getEventsAttendee(eq("event-1"), eq("ana"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotBelongToOrganizer() throws Exception {
        when(eventService.getEventByIdAndOrganizer("event-1", "organizer-1"))
                .thenThrow(new EventNotFoundException("Event with id event-1 was not found"));

        mockMvc.perform(get("/events/event-1/attendees")
                        .with(authentication(authenticationToken(organizerPrincipal()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event with id event-1 was not found"));
    }

    @Test
    void shouldReturnConflictWhenAttendeeIsAlreadyRegistered() throws Exception {
        when(eventService.registerAttendeeOnEvent(eq("event-1"), any()))
                .thenThrow(new AttendeeAlreadyExistsException("Attendee is already subscribed"));

        mockMvc.perform(post("/events/event-1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "email": "ana@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Attendee is already subscribed"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringAttendeeWithInvalidPayload() throws Exception {
        mockMvc.perform(post("/events/event-1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fields[*].field", hasItem("name")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("email")))
                .andExpect(jsonPath("$.fields[*].message", hasItem("Name is required")))
                .andExpect(jsonPath("$.fields[*].message", hasItem("Email must be valid")));
    }

    @Test
    void shouldReturnEventDetails() throws Exception {
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
                .andExpect(jsonPath("$.event.id").value("event-1"))
                .andExpect(jsonPath("$.event.numberOfAttendees").value(3))
                .andExpect(jsonPath("$.event.numberOfCheckIns").value(2));
    }

    private OrganizerPrincipal organizerPrincipal() {
        Organizer organizer = new Organizer();
        organizer.setId("organizer-1");
        organizer.setName("Gustavo");
        organizer.setEmail("gus@example.com");
        organizer.setPasswordHash("hashed-password");
        return new OrganizerPrincipal(organizer);
    }

    private UsernamePasswordAuthenticationToken authenticationToken(OrganizerPrincipal organizer) {
        return new UsernamePasswordAuthenticationToken(
                organizer,
                null,
                organizer.getAuthorities());
    }
}
