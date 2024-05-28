package br.com.nlw.passin.services;

import br.com.nlw.passin.domain.attendee.Attendee;
import br.com.nlw.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import br.com.nlw.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import br.com.nlw.passin.domain.checkin.CheckIn;
import br.com.nlw.passin.dtos.attendee.AttendeeDTO;
import br.com.nlw.passin.dtos.attendee.AttendeeListResponseDTO;
import br.com.nlw.passin.dtos.attendee.AttendeeBadgeResponseDTO;
import br.com.nlw.passin.repositories.AttendeeRepository;
import lombok.RequiredArgsConstructor;
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

    public AttendeeListResponseDTO getEventsAttendee(String eventId) {
        List<Attendee> attendeeList = getAllAttendeesFromEvent(eventId);
        List<AttendeeDTO> attendees = attendeeList.stream().map(attendee -> {
            Optional<CheckIn> checkIn = checkInService.getCheckIn(attendee.getId());
            LocalDateTime checkedInAt = checkIn.isPresent() ? checkIn.get().getCreatedAt() : null;
            return new AttendeeDTO(attendee.getId(),
                    attendee.getName(),
                    attendee.getEmail(),
                    attendee.getCreatedAt(),
                    checkedInAt);

        }).toList();
        return new AttendeeListResponseDTO(attendees);
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
}
