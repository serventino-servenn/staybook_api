package com.staybook.api.dto.reservation;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CreateHotelReservationRequest(
        @NotNull Long userId,
        @NotNull Long roomId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut
) {
}
