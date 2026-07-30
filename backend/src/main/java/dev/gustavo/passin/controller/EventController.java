package dev.gustavo.passin.controller;

import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.controller.dto.attendee.AttendeeRegistrationRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventListResponseDTO;
import dev.gustavo.passin.controller.dto.event.EventCreateRequestDTO;
import dev.gustavo.passin.controller.dto.event.EventDetailsResponseDTO;
import dev.gustavo.passin.security.OrganizerPrincipal;
import dev.gustavo.passin.service.AttendeeService;
import dev.gustavo.passin.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final AttendeeService attendeeService;

    @GetMapping
    public ResponseEntity<EventListResponseDTO> getEvents(Authentication authentication) {
        OrganizerPrincipal organizer = (OrganizerPrincipal) authentication.getPrincipal();
        EventListResponseDTO events = eventService.getEventsByOrganizer(organizer.getId());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsResponseDTO> getEvent(@PathVariable String eventId) {
        EventDetailsResponseDTO event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<String> addEvent(
            @RequestBody @Valid EventCreateRequestDTO event,
            Authentication authentication,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        OrganizerPrincipal organizer = (OrganizerPrincipal) authentication.getPrincipal();
        String id = eventService.createEventForOrganizer(event, organizer.getOrganizer());
        var uri = uriComponentsBuilder.path("/events/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(id);
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<AttendeeListResponseDTO> getEventAttendees(
            @PathVariable String eventId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String query
    ) {
        OrganizerPrincipal organizer = (OrganizerPrincipal) authentication.getPrincipal();
        eventService.getEventByIdAndOrganizer(eventId, organizer.getId());
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        AttendeeListResponseDTO attendees = attendeeService.getEventsAttendee(eventId, query, pageable);
        return ResponseEntity.ok(attendees);
    }

    @PostMapping("/{eventId}/attendees")
    public ResponseEntity<String> registerAttendee(
            @PathVariable String eventId,
            @RequestBody @Valid AttendeeRegistrationRequestDTO attendee,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        String attendeeId = eventService.registerAttendeeOnEvent(eventId, attendee);
        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/badge").buildAndExpand(attendeeId).toUri();
        return ResponseEntity.created(uri).body(attendeeId);
    }
}
