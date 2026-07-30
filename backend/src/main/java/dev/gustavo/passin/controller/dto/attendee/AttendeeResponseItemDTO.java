package dev.gustavo.passin.controller.dto.attendee;

import java.time.LocalDateTime;

public record AttendeeResponseItemDTO(
        String id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime checkInAt) {
}
