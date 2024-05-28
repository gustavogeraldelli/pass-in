package br.com.nlw.passin.controllers;

import br.com.nlw.passin.dtos.attendee.AttendeeListResponseDTO;
import br.com.nlw.passin.dtos.attendee.AttendeeRequestDTO;
import br.com.nlw.passin.dtos.event.EventRequestDTO;
import br.com.nlw.passin.dtos.event.EventResponseDTO;
import br.com.nlw.passin.services.AttendeeService;
import br.com.nlw.passin.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final AttendeeService attendeeService;

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable String eventId) {
        EventResponseDTO event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<String> addEvent(@RequestBody EventRequestDTO event, UriComponentsBuilder uriComponentsBuilder) {
        String id = eventService.createEvent(event);
        var uri = uriComponentsBuilder.path("/events/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(id);
    }

    @GetMapping("/attendees/{eventId}")
    public ResponseEntity<AttendeeListResponseDTO> getEventAttendees(@PathVariable String eventId) {
        AttendeeListResponseDTO attendees = attendeeService.getEventsAttendee(eventId);
        return ResponseEntity.ok(attendees);
    }

    @PostMapping("/{eventId}/attendees")
    public ResponseEntity<String> registerAttendee(@PathVariable String eventId, @RequestBody AttendeeRequestDTO attendee, UriComponentsBuilder uriComponentsBuilder) {
        String attendeeId = eventService.registerAttendeeOnEvent(eventId, attendee);
        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/badge").buildAndExpand(attendeeId).toUri();
        return ResponseEntity.created(uri).body(attendeeId);
    }
}
