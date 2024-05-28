package br.com.nlw.passin.dtos.event;

public record EventRequestDTO(String title,
                              String details,
                              Integer maximumAttendees) {
}
