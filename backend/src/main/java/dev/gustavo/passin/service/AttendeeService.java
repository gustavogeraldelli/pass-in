package dev.gustavo.passin.service;

import dev.gustavo.passin.entity.Attendee;
import dev.gustavo.passin.exception.AttendeeAlreadyExistsException;
import dev.gustavo.passin.exception.AttendeeNotFoundException;
import dev.gustavo.passin.entity.CheckIn;
import dev.gustavo.passin.controller.dto.attendee.AttendeeResponseItemDTO;
import dev.gustavo.passin.controller.dto.attendee.AttendeeListResponseDTO;
import dev.gustavo.passin.controller.dto.attendee.AttendeeBadgeResponseDTO;
import dev.gustavo.passin.repository.AttendeeRepository;
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
    private final CheckInTokenService checkInTokenService;

    public List<Attendee> getAllAttendeesFromEvent(String eventId) {
        return attendeeRepository.findByEventId(eventId);
    }

    public Integer countAttendeesFromEvent(String eventId) {
        return Math.toIntExact(attendeeRepository.countByEventId(eventId));
    }

    public AttendeeListResponseDTO getEventsAttendee(String eventId, String query, Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        Page<Attendee> attendeePage = attendeeRepository.findByEventIdAndQuery(eventId, normalizedQuery, pageable);
        List<AttendeeResponseItemDTO> attendees = attendeePage.stream().map(this::toResponseItem).toList();

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
        String checkInToken = checkInTokenService.generateToken(attendee.getId());
        var uri = uriComponentsBuilder.path("/check-ins/{token}").buildAndExpand(checkInToken).toUri().toString();
        return new AttendeeBadgeResponseDTO(attendee.getName(),
                attendee.getEmail(),
                uri,
                checkInToken,
                attendee.getEvent().getId());
    }

    public void checkInAttendee(String attendeeId) {
        Attendee attendee = getAttendee(attendeeId);
        checkInService.checkIn(attendee);
    }

    public void checkInAttendeeByToken(String token) {
        String attendeeId = checkInTokenService.getAttendeeId(token);
        checkInAttendee(attendeeId);
    }

    private Attendee getAttendee(String attendeeId) {
        return attendeeRepository.findById(attendeeId).orElseThrow(() -> new AttendeeNotFoundException("Attendee with id " + attendeeId + " was not found"));
    }

    private AttendeeResponseItemDTO toResponseItem(Attendee attendee) {
        Optional<CheckIn> checkIn = checkInService.getCheckIn(attendee.getId());
        LocalDateTime checkedInAt = checkIn.map(CheckIn::getCreatedAt).orElse(null);

        return new AttendeeResponseItemDTO(
                attendee.getId(),
                attendee.getName(),
                attendee.getEmail(),
                attendee.getCreatedAt(),
                checkedInAt);
    }
}
