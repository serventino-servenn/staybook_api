package com.staybook.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staybook.api.dto.mapper.ReservationMapper;
import com.staybook.api.dto.reservation.CreateEventReservationRequest;
import com.staybook.api.dto.reservation.CreateHotelReservationRequest;
import com.staybook.api.dto.reservation.ReservationResponse;
import com.staybook.api.entity.Reservation;
import com.staybook.api.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @PostMapping("/hotel")
    public ResponseEntity<ReservationResponse> createHotelReservation(
            @Valid @RequestBody CreateHotelReservationRequest request) {

        Reservation reservation = reservationService.createHotelReservation(
                request.userId(),
                request.roomId(),
                request.checkIn(),
                request.checkOut()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationMapper.toResponse(reservation));
    }

    @PostMapping("/event")
    public ResponseEntity<ReservationResponse> createEventReservation(
            @Valid @RequestBody CreateEventReservationRequest request) {

        Reservation reservation = reservationService.createEventReservation(
                request.userId(),
                request.eventId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationMapper.toResponse(reservation));
    }
}
