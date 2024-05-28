package br.com.nlw.passin.services;

import br.com.nlw.passin.domain.attendee.Attendee;
import br.com.nlw.passin.domain.event.Event;
import br.com.nlw.passin.domain.event.exceptions.EventIsFullException;
import br.com.nlw.passin.domain.event.exceptions.EventNotFoundException;
import br.com.nlw.passin.dtos.attendee.AttendeeRequestDTO;
import br.com.nlw.passin.dtos.event.EventRequestDTO;
import br.com.nlw.passin.dtos.event.EventResponseDTO;
import br.com.nlw.passin.repositories.EventRepository;
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

    public EventResponseDTO getEvent(String eventId) {
        Event event = getEventById(eventId);
        List<Attendee> attendeeList = attendeeService.getAllAttendeesFromEvent(eventId);
        return new EventResponseDTO(event, attendeeList.size());
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

    private Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " was not found"));
    }

}
