package dev.gustavo.passin.controller.dto.event;

import java.util.List;

public record EventListResponseDTO(
        List<EventResponseItemDTO> events) {
}
