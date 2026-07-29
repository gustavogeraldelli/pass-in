package dev.gustavo.passin.controller.advice;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<FieldErrorDTO> fields) {
}
