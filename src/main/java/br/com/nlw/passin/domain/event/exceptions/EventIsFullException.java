package br.com.nlw.passin.domain.event.exceptions;

public class EventIsFullException extends RuntimeException {

    public EventIsFullException(String message) {
        super(message);
    }

}
