package dev.gustavo.passin.controller.dto.attendee;

public record AttendeeBadgeResponseDTO(
        String name,
        String email,
        String checkInUrl,
        String checkInToken,
        String eventId) {
}
