package dev.gustavo.passin.exception;

public class EventIsFullException extends RuntimeException {

    public EventIsFullException(String message) {
        super(message);
    }

}
