package br.com.nlw.passin.services;

import br.com.nlw.passin.domain.attendee.Attendee;
import br.com.nlw.passin.domain.checkin.exceptions.AttendeeAlreadyCheckedInException;
import br.com.nlw.passin.domain.checkin.CheckIn;
import br.com.nlw.passin.repositories.CheckInRepository;
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
}
