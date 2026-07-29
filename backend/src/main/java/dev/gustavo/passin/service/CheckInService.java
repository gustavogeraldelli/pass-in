package dev.gustavo.passin.service;

import dev.gustavo.passin.entity.Attendee;
import dev.gustavo.passin.exception.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.entity.CheckIn;
import dev.gustavo.passin.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;

    public void checkIn(Attendee attendee) {
        verifyCheckIn(attendee.getId());
        CheckIn checkIn = new CheckIn();
        checkIn.setAttendee(attendee);
        checkIn.setCreatedAt(LocalDateTime.now());
        checkInRepository.save(checkIn);
    }

    private void verifyCheckIn(String attendeeId) {
        Optional<CheckIn> checkIn = checkInRepository.findByAttendeeId(attendeeId);
        if (checkIn.isPresent())
            throw new AttendeeAlreadyCheckedInException("This attendee is already checked in");
    }

    public Optional<CheckIn> getCheckIn(String attendeeId) {
        return checkInRepository.findByAttendeeId(attendeeId);
    }

    public Integer countCheckInsFromEvent(String eventId) {
        return Math.toIntExact(checkInRepository.countByAttendeeEventId(eventId));
    }
}
