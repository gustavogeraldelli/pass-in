package dev.gustavo.passin.controller.dto.attendee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AttendeeRequestDTO(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String email) {
}
