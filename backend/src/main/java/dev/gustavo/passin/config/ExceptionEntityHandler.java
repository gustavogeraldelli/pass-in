package dev.gustavo.passin.config;

import dev.gustavo.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import dev.gustavo.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import dev.gustavo.passin.domain.checkin.exceptions.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.domain.event.exceptions.EventIsFullException;
import dev.gustavo.passin.domain.event.exceptions.EventNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionEntityHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity handleEventNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AttendeeNotFoundException.class)
    public ResponseEntity handleAttendeeNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AttendeeAlreadyExistsException.class)
    public ResponseEntity handleAttendeeAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(AttendeeAlreadyCheckedInException.class)
    public ResponseEntity handleAttendeeAlreadyCheckedIn() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(EventIsFullException.class)
    public ResponseEntity handleEventIsFull(EventIsFullException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
