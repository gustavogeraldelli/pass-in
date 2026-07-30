package dev.gustavo.passin.controller.advice;

import dev.gustavo.passin.exception.AttendeeAlreadyExistsException;
import dev.gustavo.passin.exception.AttendeeNotFoundException;
import dev.gustavo.passin.exception.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.exception.EventIsFullException;
import dev.gustavo.passin.exception.EventNotFoundException;
import dev.gustavo.passin.exception.InvalidCheckInTokenException;
import dev.gustavo.passin.exception.InvalidRefreshTokenException;
import dev.gustavo.passin.exception.OrganizerAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleEventNotFound(
            EventNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeNotFound(
            AttendeeNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeAlreadyExists(
            AttendeeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AttendeeAlreadyCheckedInException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAttendeeAlreadyCheckedIn(
            AttendeeAlreadyCheckedInException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(EventIsFullException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleEventIsFull(
            EventIsFullException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidCheckInTokenException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidCheckInToken(
            InvalidCheckInTokenException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(OrganizerAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleOrganizerAlreadyExists(
            OrganizerAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid credentials", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
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
    public ResponseEntity<ApiErrorResponseDTO> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    private ResponseEntity<ApiErrorResponseDTO> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
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
