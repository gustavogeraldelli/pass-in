package dev.gustavo.passin.controller.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventRequestDTO(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 255)
        String details,

        @NotNull
        @Positive
        Integer maximumAttendees) {
}
