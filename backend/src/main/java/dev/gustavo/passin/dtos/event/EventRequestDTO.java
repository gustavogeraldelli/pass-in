package dev.gustavo.passin.dtos.event;

public record EventRequestDTO(String title,
                              String details,
                              Integer maximumAttendees) {
}
