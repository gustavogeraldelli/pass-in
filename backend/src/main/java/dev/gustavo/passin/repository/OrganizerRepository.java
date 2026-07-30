package dev.gustavo.passin.repository;

import dev.gustavo.passin.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerRepository extends JpaRepository<Organizer, String> {

    Optional<Organizer> findByEmail(String email);
}
