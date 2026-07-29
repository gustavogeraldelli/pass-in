package dev.gustavo.passin.controller.dto.attendee;

import java.util.List;

public record AttendeeListResponseDTO(List<AttendeeDTO> attendees,
                                      Integer page,
                                      Integer size,
                                      Long totalElements,
                                      Integer totalPages) {
}
