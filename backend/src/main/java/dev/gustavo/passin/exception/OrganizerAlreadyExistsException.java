package dev.gustavo.passin.exception;

public class OrganizerAlreadyExistsException extends RuntimeException {

    public OrganizerAlreadyExistsException(String message) {
        super(message);
    }
}
