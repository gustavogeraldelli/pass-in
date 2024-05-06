package br.com.nlw.passin.services;

import br.com.nlw.passin.domain.attendee.Attendee;
import br.com.nlw.passin.domain.checkin.CheckIn;
import br.com.nlw.passin.dtos.attendee.AttendeeDTO;
import br.com.nlw.passin.dtos.attendee.AttendeeListResponseDTO;
import br.com.nlw.passin.repositories.AttendeeRepository;
import br.com.nlw.passin.repositories.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository repository;
    private final CheckInRepository checkInRepository;

    public List<Attendee> getAllAttendeesFromEvent(String eventId) {
        return repository.findByEventId(eventId);
    }

    public AttendeeListResponseDTO getEventsAttendee(String eventId) {
        List<Attendee> attendeeList = getAllAttendeesFromEvent(eventId);
        List<AttendeeDTO> attendees = attendeeList.stream().map(attendee -> {
            Optional<CheckIn> checkIn = checkInRepository.findByAttendeeId(attendee.getId());
            LocalDateTime checkedInAt = checkIn.isPresent() ? checkIn.get().getCreatedAt() : null;
            return new AttendeeDTO(attendee.getId(),
                    attendee.getName(),
                    attendee.getEmail(),
                    attendee.getCreatedAt(),
                    checkedInAt);

        }).toList();
        return new AttendeeListResponseDTO(attendees);
    }
}
