package dev.gustavo.passin.exception;

public class AttendeeAlreadyCheckedInException extends RuntimeException{

    public AttendeeAlreadyCheckedInException(String message) {
        super(message);
    }

}
