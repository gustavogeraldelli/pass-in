package br.com.nlw.passin.dtos.attendee;

public record AttendeeBadgeResponseDTO(String name,
                                       String email,
                                       String checkInUrl,
                                       String eventId) {
}
