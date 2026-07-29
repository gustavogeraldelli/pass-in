package dev.gustavo.passin.controller.dto.event;

public record EventDTO(String id,
                       String title,
                       String details,
                       String slug,
                       Integer maximumAttendees,
                       Integer numberOfAttendees,
                       Integer numberOfCheckIns) {
}
