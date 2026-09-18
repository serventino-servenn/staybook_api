package com.staybook.api.dto.reservation;


import jakarta.validation.constraints.NotNull;

public record CreateEventReservationRequest(
        @NotNull Long userId,
        @NotNull Long eventId
) {
}

