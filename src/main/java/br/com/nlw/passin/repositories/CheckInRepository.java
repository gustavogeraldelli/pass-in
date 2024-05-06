package br.com.nlw.passin.repositories;

import br.com.nlw.passin.domain.checkin.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    public Optional<CheckIn> findByAttendeeId(String attendeeId);

}
