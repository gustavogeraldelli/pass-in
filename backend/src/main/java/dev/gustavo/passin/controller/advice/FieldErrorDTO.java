package dev.gustavo.passin.controller.advice;

public record FieldErrorDTO(
        String field,
        String message) {
}
