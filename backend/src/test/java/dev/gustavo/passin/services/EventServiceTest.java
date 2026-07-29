package dev.gustavo.passin.services;

import dev.gustavo.passin.domain.attendee.Attendee;
import dev.gustavo.passin.domain.event.Event;
import dev.gustavo.passin.domain.event.exceptions.EventIsFullException;
import dev.gustavo.passin.domain.event.exceptions.EventNotFoundException;
import dev.gustavo.passin.dtos.attendee.AttendeeRequestDTO;
import dev.gustavo.passin.dtos.event.EventRequestDTO;
import dev.gustavo.passin.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendeeService attendeeService;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldCreateEventWithGeneratedSlug() {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId("event-1");
            return event;
        });

        String eventId = eventService.createEvent(new EventRequestDTO(
                "Sao Paulo Dev Week",
                "Backend and frontend event",
                100));

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();

        assertThat(eventId).isEqualTo("event-1");
        assertThat(savedEvent.getTitle()).isEqualTo("Sao Paulo Dev Week");
        assertThat(savedEvent.getSlug()).isEqualTo("sao-paulo-dev-week");
        assertThat(savedEvent.getMaximumAttendees()).isEqualTo(100);
    }

    @Test
    void shouldThrowWhenEventDoesNotExist() {
        when(eventRepository.findById("missing-event")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById("missing-event"))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event with id missing-event was not found");
    }

    @Test
    void shouldPreventRegistrationWhenEventIsFull() {
        Event event = event("event-1", 1);
        Attendee attendee = attendee("attendee-1", event);

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(attendeeService.getAllAttendeesFromEvent("event-1")).thenReturn(List.of(attendee));

        assertThatThrownBy(() -> eventService.registerAttendeeOnEvent(
                "event-1",
                new AttendeeRequestDTO("Ana", "ana@example.com")))
                .isInstanceOf(EventIsFullException.class)
                .hasMessage("Event is full");

        verify(attendeeService).verifyAttendeeSubscription("ana@example.com", "event-1");
        verify(attendeeService, never()).registerAttendee(any(Attendee.class));
    }

    @Test
    void shouldRegisterAttendeeOnEventWhenCapacityIsAvailable() {
        Event event = event("event-1", 2);

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(attendeeService.getAllAttendeesFromEvent("event-1")).thenReturn(List.of());
        when(attendeeService.registerAttendee(any(Attendee.class))).thenAnswer(invocation -> {
            Attendee attendee = invocation.getArgument(0);
            attendee.setId("attendee-1");
            return attendee;
        });

        String attendeeId = eventService.registerAttendeeOnEvent(
                "event-1",
                new AttendeeRequestDTO("Ana", "ana@example.com"));

        ArgumentCaptor<Attendee> attendeeCaptor = ArgumentCaptor.forClass(Attendee.class);
        verify(attendeeService).registerAttendee(attendeeCaptor.capture());
        Attendee savedAttendee = attendeeCaptor.getValue();

        assertThat(attendeeId).isEqualTo("attendee-1");
        assertThat(savedAttendee.getName()).isEqualTo("Ana");
        assertThat(savedAttendee.getEmail()).isEqualTo("ana@example.com");
        assertThat(savedAttendee.getEvent()).isEqualTo(event);
        assertThat(savedAttendee.getCreatedAt()).isNotNull();
    }

    private Event event(String id, Integer maximumAttendees) {
        Event event = new Event();
        event.setId(id);
        event.setTitle("Java Conf");
        event.setDetails("Conference");
        event.setSlug("java-conf");
        event.setMaximumAttendees(maximumAttendees);
        return event;
    }

    private Attendee attendee(String id, Event event) {
        Attendee attendee = new Attendee();
        attendee.setId(id);
        attendee.setName("Gustavo");
        attendee.setEmail("gus@example.com");
        attendee.setEvent(event);
        return attendee;
    }
}
