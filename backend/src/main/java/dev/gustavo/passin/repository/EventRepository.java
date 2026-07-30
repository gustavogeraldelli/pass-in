package dev.gustavo.passin.repository;

import dev.gustavo.passin.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findAllByOrganizerId(String organizerId);

    Optional<Event> findByIdAndOrganizerId(String eventId, String organizerId);
}
