package dev.gustavo.passin.services;

import dev.gustavo.passin.domain.attendee.Attendee;
import dev.gustavo.passin.domain.checkin.CheckIn;
import dev.gustavo.passin.domain.checkin.exceptions.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.repositories.CheckInRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private CheckInService checkInService;

    @Test
    void shouldCreateCheckInWhenAttendeeHasNotCheckedInYet() {
        Attendee attendee = attendee("attendee-1");
        when(checkInRepository.findByAttendeeId("attendee-1")).thenReturn(Optional.empty());

        checkInService.checkIn(attendee);

        ArgumentCaptor<CheckIn> checkInCaptor = ArgumentCaptor.forClass(CheckIn.class);
        verify(checkInRepository).save(checkInCaptor.capture());
        CheckIn savedCheckIn = checkInCaptor.getValue();

        assertThat(savedCheckIn.getAttendee()).isEqualTo(attendee);
        assertThat(savedCheckIn.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPreventDuplicatedCheckIn() {
        Attendee attendee = attendee("attendee-1");
        when(checkInRepository.findByAttendeeId("attendee-1")).thenReturn(Optional.of(new CheckIn()));

        assertThatThrownBy(() -> checkInService.checkIn(attendee))
                .isInstanceOf(AttendeeAlreadyCheckedInException.class)
                .hasMessage("This attendee is already checked in");

        verify(checkInRepository, never()).save(any(CheckIn.class));
    }

    private Attendee attendee(String id) {
        Attendee attendee = new Attendee();
        attendee.setId(id);
        attendee.setName("Ana");
        attendee.setEmail("ana@example.com");
        return attendee;
    }
}
