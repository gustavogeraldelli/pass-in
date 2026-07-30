package dev.gustavo.passin.controller.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventCreateRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must have at most 255 characters")
        String title,

        @NotBlank(message = "Details are required")
        @Size(max = 255, message = "Details must have at most 255 characters")
        String details,

        @NotNull(message = "Maximum attendees is required")
        @Positive(message = "Maximum attendees must be greater than zero")
        Integer maximumAttendees) {
}
