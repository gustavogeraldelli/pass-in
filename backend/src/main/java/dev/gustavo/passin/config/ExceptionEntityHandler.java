package dev.gustavo.passin.config;

import dev.gustavo.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import dev.gustavo.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import dev.gustavo.passin.domain.checkin.exceptions.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.domain.event.exceptions.EventIsFullException;
import dev.gustavo.passin.domain.event.exceptions.EventNotFoundException;
import dev.gustavo.passin.dtos.error.ApiErrorResponseDTO;
import dev.gustavo.passin.dtos.error.FieldErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ExceptionEntityHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleEventNotFound(EventNotFoundException exception, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeNotFound(AttendeeNotFoundException exception, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeAlreadyExists(AttendeeAlreadyExistsException exception, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeAlreadyCheckedInException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeAlreadyCheckedIn(AttendeeAlreadyCheckedInException exception, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(EventIsFullException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleEventIsFull(EventIsFullException exception, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorDTO> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDTO(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                fields);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMalformedJson(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    private ResponseEntity<ApiErrorResponseDTO> buildError(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                List.of());

        return ResponseEntity.status(status).body(response);
    }
}
