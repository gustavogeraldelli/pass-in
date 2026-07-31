package dev.gustavo.passin.service;

import dev.gustavo.passin.entity.Attendee;
import dev.gustavo.passin.exception.AttendeeAlreadyExistsException;
import dev.gustavo.passin.exception.AttendeeNotFoundException;
import dev.gustavo.passin.entity.CheckIn;
import dev.gustavo.passin.entity.Event;
import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.repository.AttendeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendeeServiceTest {

    @Mock
    private AttendeeRepository attendeeRepository;

    @Mock
    private CheckInService checkInService;

    @Mock
    private CheckInTokenService checkInTokenService;

    @InjectMocks
    private AttendeeService attendeeService;

    @Test
    void shouldThrowWhenEmailIsAlreadyRegisteredOnEvent() {
        when(attendeeRepository.findByEventIdAndEmail("event-1", "ana@example.com"))
                .thenReturn(Optional.of(attendee("attendee-1", event("event-1"))));

        assertThatThrownBy(() -> attendeeService.verifyAttendeeSubscription("ana@example.com", "event-1"))
                .isInstanceOf(AttendeeAlreadyExistsException.class)
                .hasMessage("Attendee is already subscribed");
    }

    @Test
    void shouldCountAttendeesFromEvent() {
        when(attendeeRepository.countByEventId("event-1")).thenReturn(12L);

        Integer count = attendeeService.countAttendeesFromEvent("event-1");

        assertThat(count).isEqualTo(12);
    }

    @Test
    void shouldReturnPaginatedAttendeesWithCheckInStatus() {
        Event event = event("event-1");
        Attendee attendee = attendee("attendee-1", event);
        CheckIn checkIn = new CheckIn();
        checkIn.setCreatedAt(LocalDateTime.of(2026, 7, 28, 10, 0));

        PageRequest pageable = PageRequest.of(0, 10);
        when(attendeeRepository.findByEventIdAndQuery("event-1", "ana", pageable))
                .thenReturn(new PageImpl<>(List.of(attendee), pageable, 1));
        when(checkInService.getCheckIn("attendee-1")).thenReturn(Optional.of(checkIn));

        AttendeeListResponseDTO response = attendeeService.getEventsAttendee("event-1", "  ana  ", pageable);

        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.attendees()).hasSize(1);
        assertThat(response.attendees().getFirst().id()).isEqualTo("attendee-1");
        assertThat(response.attendees().getFirst().checkInAt()).isEqualTo(checkIn.getCreatedAt());
    }

    @Test
    void shouldReturnPaginatedAttendeesWithoutSearchQuery() {
        Event event = event("event-1");
        Attendee attendee = attendee("attendee-1", event);

        PageRequest pageable = PageRequest.of(0, 10);
        when(attendeeRepository.findByEventId("event-1", pageable))
                .thenReturn(new PageImpl<>(List.of(attendee), pageable, 1));
        when(checkInService.getCheckIn("attendee-1")).thenReturn(Optional.empty());

        AttendeeListResponseDTO response = attendeeService.getEventsAttendee("event-1", " ", pageable);

        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.attendees()).hasSize(1);
        assertThat(response.attendees().getFirst().checkInAt()).isNull();
    }

    @Test
    void shouldExportAttendeesAsCsv() {
        Event event = event("event-1");
        Attendee attendee = attendee("attendee-1", event);
        attendee.setName("Ana \"Backend\", Silva");
        CheckIn checkIn = new CheckIn();
        checkIn.setCreatedAt(LocalDateTime.of(2026, 7, 28, 10, 0));

        when(attendeeRepository.findByEventIdOrderByCreatedAtAsc("event-1")).thenReturn(List.of(attendee));
        when(checkInService.getCheckIn("attendee-1")).thenReturn(Optional.of(checkIn));

        String csv = attendeeService.exportEventAttendeesCsv("event-1");

        assertThat(csv).isEqualTo("""
                id,name,email,registeredAt,checkedInAt
                "attendee-1","Ana ""Backend"", Silva","ana@example.com","2026-07-28T09:00","2026-07-28T10:00"
                """);
    }

    @Test
    void shouldReturnAttendeeBadge() {
        Event event = event("event-1");
        Attendee attendee = attendee("attendee-1", event);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080");

        when(attendeeRepository.findById("attendee-1")).thenReturn(Optional.of(attendee));
        when(checkInTokenService.generateToken("attendee-1")).thenReturn("signed-token");

        var response = attendeeService.getAttendeeBadge("attendee-1", uriBuilder);

        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.eventId()).isEqualTo("event-1");
        assertThat(response.checkInToken()).isEqualTo("signed-token");
        assertThat(response.checkInUrl()).isEqualTo("http://localhost:8080/check-ins/signed-token");
    }

    @Test
    void shouldThrowWhenGeneratingBadgeForMissingAttendee() {
        when(attendeeRepository.findById("missing-attendee")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendeeService.getAttendeeBadge(
                "missing-attendee",
                UriComponentsBuilder.fromUriString("http://localhost:8080")))
                .isInstanceOf(AttendeeNotFoundException.class)
                .hasMessage("Attendee with id missing-attendee was not found");
    }

    @Test
    void shouldCheckInExistingAttendee() {
        Attendee attendee = attendee("attendee-1", event("event-1"));
        when(attendeeRepository.findById("attendee-1")).thenReturn(Optional.of(attendee));

        attendeeService.checkInAttendee("attendee-1");

        verify(checkInService).checkIn(attendee);
    }

    @Test
    void shouldCheckInAttendeeByToken() {
        Attendee attendee = attendee("attendee-1", event("event-1"));
        when(checkInTokenService.getAttendeeId("signed-token")).thenReturn("attendee-1");
        when(attendeeRepository.findById("attendee-1")).thenReturn(Optional.of(attendee));

        attendeeService.checkInAttendeeByToken("signed-token");

        verify(checkInService).checkIn(attendee);
    }

    private Event event(String id) {
        Event event = new Event();
        event.setId(id);
        event.setTitle("Java Conf");
        event.setDetails("Conference");
        event.setSlug("java-conf");
        event.setMaximumAttendees(100);
        return event;
    }

    private Attendee attendee(String id, Event event) {
        Attendee attendee = new Attendee();
        attendee.setId(id);
        attendee.setName("Ana");
        attendee.setEmail("ana@example.com");
        attendee.setEvent(event);
        attendee.setCreatedAt(LocalDateTime.of(2026, 7, 28, 9, 0));
        return attendee;
    }
}
