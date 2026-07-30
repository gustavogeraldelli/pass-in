package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.controller.dto.attendee.AttendeeRegistrationRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventCreateRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventDetailsResponseDTO;
import dev.gustavo.passin.service.AttendeeService;
import dev.gustavo.passin.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final AttendeeService attendeeService;

    @GetMapping
    public ResponseEntity<EventListResponseDTO> getEvents() {
        EventListResponseDTO events = eventService.getEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsResponseDTO> getEvent(@PathVariable String eventId) {
        EventDetailsResponseDTO event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<String> addEvent(@RequestBody @Valid EventCreateRequestDTO event, UriComponentsBuilder uriComponentsBuilder) {
        String id = eventService.createEvent(event);
        var uri = uriComponentsBuilder.path("/events/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(id);
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<AttendeeListResponseDTO> getEventAttendees(@PathVariable String eventId,
                                                                     @RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size,
                                                                     @RequestParam(required = false) String query) {
        eventService.getEventById(eventId);
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        AttendeeListResponseDTO attendees = attendeeService.getEventsAttendee(eventId, query, pageable);
        return ResponseEntity.ok(attendees);
    }

    @PostMapping("/{eventId}/attendees")
    public ResponseEntity<String> registerAttendee(@PathVariable String eventId, @RequestBody @Valid AttendeeRegistrationRequestDTO attendee, UriComponentsBuilder uriComponentsBuilder) {
        String attendeeId = eventService.registerAttendeeOnEvent(eventId, attendee);
        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/badge").buildAndExpand(attendeeId).toUri();
        return ResponseEntity.created(uri).body(attendeeId);
    }

    private int normalizePage(Integer page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(Integer size) {
        return Math.min(Math.max(size, 1), 100);
    }
}
