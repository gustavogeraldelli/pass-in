package dev.gustavo.passin.controllers;

import dev.gustavo.passin.dtos.attendee.AttendeeBadgeResponseDTO;
import dev.gustavo.passin.services.AttendeeService;
import dev.gustavo.passin.services.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/attendees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:9090")
public class AttendeeController {

    private final AttendeeService service;

    @GetMapping("/{attendeeId}/badge")
    public ResponseEntity<AttendeeBadgeResponseDTO> getBadge(@PathVariable String attendeeId, UriComponentsBuilder uriComponentsBuilder) {
        AttendeeBadgeResponseDTO response = service.getAttendeeBadge(attendeeId, uriComponentsBuilder);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{attendeeId}/check-in")
    public ResponseEntity checkInAttendee(@PathVariable String attendeeId, UriComponentsBuilder uriComponentsBuilder) {
        service.checkInAttendee(attendeeId);
        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/badge").buildAndExpand(attendeeId).toUri();
        return ResponseEntity.created(uri).build();
    }

}
