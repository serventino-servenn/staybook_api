package com.staybook.api.dto.event;

import com.staybook.api.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse(

        Long id,

        String name,

        String description,

        String venue,

        LocalDateTime eventDate,

        Integer capacity,

        Integer availableSeats,

        BigDecimal price,

        EventStatus status,

        LocalDateTime createdAt

) {
}
