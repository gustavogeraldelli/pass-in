package dev.gustavo.passin.service;

import dev.gustavo.passin.entity.Attendee;
import dev.gustavo.passin.entity.Event;
import dev.gustavo.passin.exception.EventIsFullException;
import dev.gustavo.passin.exception.EventNotFoundException;
import dev.gustavo.passin.controller.dto.attendee.AttendeeRegistrationRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventResponseItemDTO;
import dev.gustavo.passin.controller.dto.event.EventListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventCreateRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventDetailsResponseDTO;
import dev.gustavo.passin.repository.EventRepository;
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
        List<EventResponseItemDTO> events = eventRepository.findAll()
                .stream()
                .map(event -> new EventResponseItemDTO(
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

    public EventDetailsResponseDTO getEvent(String eventId) {
        Event event = getEventById(eventId);
        Integer attendeeCount = attendeeService.countAttendeesFromEvent(eventId);
        Integer checkInCount = checkInService.countCheckInsFromEvent(eventId);
        EventResponseItemDTO responseItem = new EventResponseItemDTO(
                event.getId(),
                event.getTitle(),
                event.getDetails(),
                event.getSlug(),
                event.getMaximumAttendees(),
                attendeeCount,
                checkInCount);

        return new EventDetailsResponseDTO(responseItem);
    }

    public String createEvent(EventCreateRequestDTO eventRequestDTO) {
        Event event = new Event();
        event.setTitle(eventRequestDTO.title());
        event.setDetails(eventRequestDTO.details());
        event.setSlug(generateSlug(eventRequestDTO.title()));
        event.setMaximumAttendees(eventRequestDTO.maximumAttendees());
        eventRepository.save(event);
        return event.getId();
    }

    public String registerAttendeeOnEvent(String eventId, AttendeeRegistrationRequestDTO attendeeRequestDTO) {
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
