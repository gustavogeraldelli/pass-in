package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventResponseDTO;
import dev.gustavo.passin.entity.Event;
import dev.gustavo.passin.exception.AttendeeAlreadyExistsException;
import dev.gustavo.passin.exception.EventNotFoundException;
import dev.gustavo.passin.service.AttendeeService;
import dev.gustavo.passin.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private AttendeeService attendeeService;

    @Test
    void shouldReturnEvents() throws Exception {
        when(eventService.getEvents()).thenReturn(new EventListResponseDTO(List.of()));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray());
    }

    @Test
    void shouldCreateEvent() throws Exception {
        when(eventService.createEvent(any())).thenReturn("event-1");

        mockMvc.perform(post("/events")
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
    }

    @Test
    void shouldReturnBadRequestWhenCreatingEventWithInvalidPayload() throws Exception {
        mockMvc.perform(post("/events")
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
                .andExpect(jsonPath("$.fields[*].field", hasItem("maximumAttendees")));
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
                        .param("page", "-1")
                        .param("size", "200")
                        .param("query", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        var pageableCaptor = forClass(Pageable.class);

        verify(eventService).getEventById("event-1");
        verify(attendeeService).getEventsAttendee(eq("event-1"), eq("ana"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
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
    void shouldReturnEventDetails() throws Exception {
        when(eventService.getEvent("event-1")).thenReturn(new EventResponseDTO(event(), 3, 2));

        mockMvc.perform(get("/events/event-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.id").value("event-1"))
                .andExpect(jsonPath("$.event.numberOfAttendees").value(3))
                .andExpect(jsonPath("$.event.numberOfCheckIns").value(2));
    }

    private Event event() {
        Event event = new Event();
        event.setId("event-1");
        event.setTitle("Java Conf");
        event.setDetails("Backend event");
        event.setSlug("java-conf");
        event.setMaximumAttendees(100);
        return event;
    }
}
