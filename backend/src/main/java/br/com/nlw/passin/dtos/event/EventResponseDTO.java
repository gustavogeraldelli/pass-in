package br.com.nlw.passin.dtos.event;

import br.com.nlw.passin.domain.event.Event;
import lombok.Getter;

@Getter
public class EventResponseDTO {

    EventDTO event;

    public EventResponseDTO(Event event, Integer numberOfAttendees) {
        this.event = new EventDTO(event.getId(),
                event.getTitle(),
                event.getDetails(),
                event.getSlug(),
                event.getMaximumAttendees(),
                numberOfAttendees);
    }
}
