package dev.gustavo.passin.services;

import dev.gustavo.passin.domain.attendee.Attendee;
import dev.gustavo.passin.domain.event.Event;
import dev.gustavo.passin.domain.event.exceptions.EventIsFullException;
import dev.gustavo.passin.domain.event.exceptions.EventNotFoundException;
import dev.gustavo.passin.dtos.attendee.AttendeeRequestDTO;
import dev.gustavo.passin.dtos.event.EventDTO;
import dev.gustavo.passin.dtos.event.EventListResponseDTO;
import dev.gustavo.passin.dtos.event.EventRequestDTO;
import dev.gustavo.passin.dtos.event.EventResponseDTO;
import dev.gustavo.passin.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AttendeeService attendeeService;
    private final CheckInService checkInService;

    public EventListResponseDTO getEvents() {
        List<EventDTO> events = eventRepository.findAll()
                .stream()
                .map(event -> new EventDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDetails(),
                        event.getSlug(),
                        event.getMaximumAttendees(),
                        attendeeService.countAttendeesFromEvent(event.getId()),
                        checkInService.countCheckInsFromEvent(event.getId())))
                .toList();

        return new EventListResponseDTO(events);
    }

    public EventResponseDTO getEvent(String eventId) {
        Event event = getEventById(eventId);
        Integer attendeeCount = attendeeService.countAttendeesFromEvent(eventId);
        Integer checkInCount = checkInService.countCheckInsFromEvent(eventId);
        return new EventResponseDTO(event, attendeeCount, checkInCount);
    }

    public String createEvent(EventRequestDTO eventRequestDTO) {
        Event event = new Event();
        event.setTitle(eventRequestDTO.title());
        event.setDetails(eventRequestDTO.details());
        event.setSlug(generateSlug(eventRequestDTO.title()));
        event.setMaximumAttendees(eventRequestDTO.maximumAttendees());
        eventRepository.save(event);
        return event.getId();
    }

    public String registerAttendeeOnEvent(String eventId, AttendeeRequestDTO attendeeRequestDTO) {
        attendeeService.verifyAttendeeSubscription(attendeeRequestDTO.email(), eventId);
        Event event = getEventById(eventId);
        List<Attendee> attendeeList = attendeeService.getAllAttendeesFromEvent(eventId);

        if (event.getMaximumAttendees() <= attendeeList.size())
            throw new EventIsFullException("Event is full");

        Attendee attendee = new Attendee();
        attendee.setName(attendeeRequestDTO.name());
        attendee.setEmail(attendeeRequestDTO.email());
        attendee.setEvent(event);
        attendee.setCreatedAt(LocalDateTime.now());
        attendeeService.registerAttendee(attendee);
        return attendee.getId();
    }

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "")
                .replaceAll("[^\\w\\s]", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " was not found"));
    }

}
