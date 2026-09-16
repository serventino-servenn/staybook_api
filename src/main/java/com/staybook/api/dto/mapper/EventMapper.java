package com.staybook.api.dto.mapper;

import org.springframework.stereotype.Component;

import com.staybook.api.dto.event.CreateEventRequest;
import com.staybook.api.dto.event.EventResponse;
import com.staybook.api.entity.Event;

@Component
public class EventMapper {

   public Event toEntity(CreateEventRequest request){
     return Event.builder()
             .name(request.name())
             .description(request.description())
             .venue(request.venue())
             .eventDate(request.eventDate())
             .capacity(request.capacity())
             .price(request.price())
             .build();
   }

   public EventResponse toResponse(Event event){
     return new EventResponse(
             event.getId(),
             event.getName(),
             event.getDescription(),
             event.getVenue(),
             event.getEventDate(),
             event.getCapacity(),
             event.getAvailableSeats(),
             event.getPrice(),
             event.getStatus(),
             event.getCreatedAt()
     );
   }
    
}
