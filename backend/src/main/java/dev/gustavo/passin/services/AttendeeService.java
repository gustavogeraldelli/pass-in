package dev.gustavo.passin.services;

import dev.gustavo.passin.domain.attendee.Attendee;
import dev.gustavo.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import dev.gustavo.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import dev.gustavo.passin.domain.checkin.CheckIn;
import dev.gustavo.passin.dtos.attendee.AttendeeDTO;
import dev.gustavo.passin.dtos.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.dtos.attendee.AttendeeBadgeResponseDTO;
import dev.gustavo.passin.repositories.AttendeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final CheckInService checkInService;

    public List<Attendee> getAllAttendeesFromEvent(String eventId) {
        return attendeeRepository.findByEventId(eventId);
    }

    public Integer countAttendeesFromEvent(String eventId) {
        return attendeeRepository.countByEventId(eventId);
    }

    public AttendeeListResponseDTO getEventsAttendee(String eventId, String query, Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        Page<Attendee> attendeePage = attendeeRepository.findByEventIdAndQuery(eventId, normalizedQuery, pageable);
        List<AttendeeDTO> attendees = attendeePage.stream().map(this::toDTO).toList();

        return new AttendeeListResponseDTO(
                attendees,
                attendeePage.getNumber(),
                attendeePage.getSize(),
                attendeePage.getTotalElements(),
                attendeePage.getTotalPages());
    }

    public void verifyAttendeeSubscription(String email, String eventId) {
        Optional<Attendee> attendee = attendeeRepository.findByEventIdAndEmail(eventId, email);
        if (attendee.isPresent())
            throw new AttendeeAlreadyExistsException("Attendee is already subscribed");
    }

    public Attendee registerAttendee(Attendee attendee) {
        attendeeRepository.save(attendee);
        return attendee;
    }

    public AttendeeBadgeResponseDTO getAttendeeBadge(String attendeeId, UriComponentsBuilder uriComponentsBuilder) {
        Attendee attendee = getAttendee(attendeeId);
        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/check-in").buildAndExpand(attendeeId).toUri().toString();
        return new AttendeeBadgeResponseDTO(attendee.getName(),
                attendee.getEmail(),
                uri,
                attendee.getEvent().getId());
    }

    public void checkInAttendee(String attendeeId) {
        Attendee attendee = getAttendee(attendeeId);
        checkInService.checkIn(attendee);
    }

    private Attendee getAttendee(String attendeeId) {
        return attendeeRepository.findById(attendeeId).orElseThrow(() -> new AttendeeNotFoundException("Attendee with id " + attendeeId + " was not found"));
    }

    private AttendeeDTO toDTO(Attendee attendee) {
        Optional<CheckIn> checkIn = checkInService.getCheckIn(attendee.getId());
        LocalDateTime checkedInAt = checkIn.map(CheckIn::getCreatedAt).orElse(null);

        return new AttendeeDTO(
                attendee.getId(),
                attendee.getName(),
                attendee.getEmail(),
                attendee.getCreatedAt(),
                checkedInAt);
    }
}
