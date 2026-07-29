package dev.gustavo.passin.repository;

import dev.gustavo.passin.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    Optional<CheckIn> findByAttendeeId(String attendeeId);

    Long countByAttendeeEventId(String eventId);

}
