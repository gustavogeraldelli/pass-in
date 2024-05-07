package br.com.nlw.passin.domain.checkin.exceptions;

public class AttendeeAlreadyCheckedInException extends RuntimeException{

    public AttendeeAlreadyCheckedInException(String message) {
        super(message);
    }

}
