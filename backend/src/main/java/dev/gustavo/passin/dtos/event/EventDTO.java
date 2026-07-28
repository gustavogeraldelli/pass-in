package dev.gustavo.passin.dtos.event;

public record EventDTO(String id,
                       String title,
                       String details,
                       String slug,
                       Integer maximumAttendees,
                       Integer numberOfAttendees) {
}
