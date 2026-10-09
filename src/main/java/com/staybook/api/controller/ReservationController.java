package com.staybook.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   @PreAuthorize("hasRole('CUSTOMER')")
        public ResponseEntity<ReservationResponse> createHotelReservation(
                @Valid @RequestBody CreateHotelReservationRequest request,
                @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation =
                reservationService.createHotelReservation(
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationMapper.toResponse(reservation));
        }

    @PostMapping("/event")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponse> createEventReservation(
            @Valid @RequestBody CreateEventReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation =
                reservationService.createEventReservation(
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationMapper.toResponse(reservation));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
        public ResponseEntity<List<ReservationResponse>> getMyReservations(
                @AuthenticationPrincipal UserDetails userDetails) {

        List<Reservation> reservations =
                reservationService.getReservationsByUser(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                reservations.stream()
                        .map(reservationMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
        public ResponseEntity<ReservationResponse> getReservationById(
                @PathVariable Long id,
                @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation =
                reservationService.getReservationById(
                        id,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                reservationMapper.toResponse(reservation)
        );
     }

     @PatchMapping("/{id}/cancel")
     @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
        public ResponseEntity<ReservationResponse> cancelReservation(
                @PathVariable Long id,
                @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation = reservationService.cancelReservation(
                id,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
      }
}
