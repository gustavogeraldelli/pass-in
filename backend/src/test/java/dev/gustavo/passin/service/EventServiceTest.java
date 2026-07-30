package dev.gustavo.passin.service;

import dev.gustavo.passin.entity.Attendee;
import dev.gustavo.passin.entity.Event;
import dev.gustavo.passin.entity.Organizer;
import dev.gustavo.passin.exception.EventIsFullException;
import dev.gustavo.passin.exception.EventNotFoundException;
import dev.gustavo.passin.controller.dto.attendee.AttendeeRegistrationRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventCreateRequestDTO;
import dev.gustavo.passin.repository.EventRepository;
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

    @Mock
    private CheckInService checkInService;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldCreateEventForOrganizerWithGeneratedSlug() {
        Organizer organizer = organizer("organizer-1");
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId("event-1");
            return event;
        });

        String eventId = eventService.createEventForOrganizer(new EventCreateRequestDTO(
                "Java Conf",
                "Backend event",
                100), organizer);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();

        assertThat(eventId).isEqualTo("event-1");
        assertThat(savedEvent.getTitle()).isEqualTo("Java Conf");
        assertThat(savedEvent.getSlug()).isEqualTo("java-conf");
        assertThat(savedEvent.getMaximumAttendees()).isEqualTo(100);
        assertThat(savedEvent.getOrganizer()).isEqualTo(organizer);
    }

    @Test
    void shouldReturnEventsByOrganizer() {
        Event event = event("event-1", 100);
        event.setOrganizer(organizer("organizer-1"));
        when(eventRepository.findAllByOrganizerId("organizer-1")).thenReturn(List.of(event));
        when(attendeeService.countAttendeesFromEvent("event-1")).thenReturn(3);
        when(checkInService.countCheckInsFromEvent("event-1")).thenReturn(2);

        var response = eventService.getEventsByOrganizer("organizer-1");

        assertThat(response.events()).hasSize(1);
        assertThat(response.events().getFirst().id()).isEqualTo("event-1");
        assertThat(response.events().getFirst().numberOfAttendees()).isEqualTo(3);
        assertThat(response.events().getFirst().numberOfCheckIns()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenEventDoesNotExist() {
        when(eventRepository.findById("missing-event")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById("missing-event"))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event with id missing-event was not found");
    }

    @Test
    void shouldThrowWhenOrganizerEventDoesNotExist() {
        when(eventRepository.findByIdAndOrganizerId("event-1", "organizer-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByIdAndOrganizer("event-1", "organizer-1"))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event with id event-1 was not found");
    }

    @Test
    void shouldReturnOrganizerEvent() {
        Event event = event("event-1", 100);
        when(eventRepository.findByIdAndOrganizerId("event-1", "organizer-1")).thenReturn(Optional.of(event));

        Event response = eventService.getEventByIdAndOrganizer("event-1", "organizer-1");

        assertThat(response).isEqualTo(event);
    }

    @Test
    void shouldPreventRegistrationWhenEventIsFull() {
        Event event = event("event-1", 1);
        Attendee attendee = attendee("attendee-1", event);

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(attendeeService.getAllAttendeesFromEvent("event-1")).thenReturn(List.of(attendee));

        assertThatThrownBy(() -> eventService.registerAttendeeOnEvent(
                "event-1",
                new AttendeeRegistrationRequestDTO("Ana", "ana@example.com")))
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
                new AttendeeRegistrationRequestDTO("Ana", "ana@example.com"));

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

    private Organizer organizer(String id) {
        Organizer organizer = new Organizer();
        organizer.setId(id);
        organizer.setName("Gustavo");
        organizer.setEmail("gus@example.com");
        organizer.setPasswordHash("hashed-password");
        return organizer;
    }
}
