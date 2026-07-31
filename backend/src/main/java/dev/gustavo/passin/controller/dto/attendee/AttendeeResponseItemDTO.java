package dev.gustavo.passin.controller.dto.attendee;

import java.time.OffsetDateTime;

public record AttendeeResponseItemDTO(
        String id,
        String name,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime checkInAt) {
}
