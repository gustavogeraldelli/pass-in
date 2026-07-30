package dev.gustavo.passin.repository;

import dev.gustavo.passin.entity.Attendee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, String> {
    List<Attendee> findByEventId(String eventId);
    Page<Attendee> findByEventId(String eventId, Pageable pageable);
    Optional<Attendee> findByEventIdAndEmail(String eventId, String email);
    Long countByEventId(String eventId);

    @Query("""
            SELECT attendee FROM Attendee attendee
            WHERE attendee.event.id = :eventId
              AND (
                :query IS NULL
                OR LOWER(attendee.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(attendee.email) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<Attendee> findByEventIdAndQuery(String eventId, String query, Pageable pageable);
}
