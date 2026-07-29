package dev.gustavo.passin.exception;

public class InvalidCheckInTokenException extends RuntimeException {

    public InvalidCheckInTokenException(String message) {
        super(message);
    }
}
