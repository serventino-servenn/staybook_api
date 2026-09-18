package com.staybook.api.dto.mapper;

import org.springframework.stereotype.Component;

import com.staybook.api.dto.reservation.ReservationResponse;
import com.staybook.api.entity.Reservation;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getRoom() != null ? reservation.getRoom().getId() : null,
                reservation.getEvent() != null ? reservation.getEvent().getId() : null,
                reservation.getType(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getReservedAt(),
                reservation.getStatus()
        );
    }
}
