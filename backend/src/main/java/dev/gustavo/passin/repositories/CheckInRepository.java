package dev.gustavo.passin.repositories;

import dev.gustavo.passin.domain.checkin.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    public Optional<CheckIn> findByAttendeeId(String attendeeId);

}
