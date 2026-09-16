package com.staybook.api.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateEventRequest(

        @NotBlank
        String name,

        String description,

        @NotBlank
        String venue,

        @NotNull
        LocalDateTime eventDate,

        @NotNull
        @Positive
        Integer capacity,

        @NotNull
        @Positive
        BigDecimal price

) {
}