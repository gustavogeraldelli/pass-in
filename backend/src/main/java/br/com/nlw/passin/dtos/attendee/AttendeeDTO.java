package br.com.nlw.passin.dtos.attendee;

import java.time.LocalDateTime;

public record AttendeeDTO(String id,
                          String name,
                          String email,
                          LocalDateTime createdAt,
                          LocalDateTime checkInAt) {
}
