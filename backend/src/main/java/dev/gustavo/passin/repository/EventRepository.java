package dev.gustavo.passin.repository;

import dev.gustavo.passin.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, String> {
}
