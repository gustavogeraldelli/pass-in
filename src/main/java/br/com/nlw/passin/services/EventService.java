package br.com.nlw.passin.services;

import br.com.nlw.passin.domain.attendee.Attendee;
import br.com.nlw.passin.domain.event.Event;
import br.com.nlw.passin.domain.event.exceptions.EventNotFoundException;
import br.com.nlw.passin.dtos.event.EventRequestDTO;
import br.com.nlw.passin.dtos.event.EventResponseDTO;
import br.com.nlw.passin.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AttendeeService attendeeService;

    public EventResponseDTO getEvent(String eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " not found"));
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

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "")
                .replaceAll("[^\\w\\s]", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

}
