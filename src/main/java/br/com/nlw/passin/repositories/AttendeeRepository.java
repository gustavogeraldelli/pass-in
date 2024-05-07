package br.com.nlw.passin.repositories;

import br.com.nlw.passin.domain.attendee.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, String> {
    public List<Attendee> findByEventId(String eventId);
    public Optional<Attendee> findByEventIdAndEmail(String eventId, String email);
}
