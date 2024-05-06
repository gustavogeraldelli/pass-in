package br.com.nlw.passin.dtos.event;

public record EventDTO(String id,
                       String title,
                       String details,
                       String slug,
                       Integer maximumAttendees,
                       Integer numberOfAttendees) {
}
