package br.com.nlw.passin.config;

import br.com.nlw.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import br.com.nlw.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import br.com.nlw.passin.domain.checkin.exceptions.AttendeeAlreadyCheckedInException;
import br.com.nlw.passin.domain.event.exceptions.EventIsFullException;
import br.com.nlw.passin.domain.event.exceptions.EventNotFoundException;
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
