package com.staybook.api.dto.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;

public record ReservationResponse(
        Long id,
        Long userId,
        Long roomId,
        Long eventId,
        ReservationType type,
        LocalDate checkIn,
        LocalDate checkOut,
        LocalDateTime reservedAt,
        ReservationStatus status
) {}