package com.staybook.api.service;

import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.EventRepository;
import com.staybook.api.repository.ReservationRepository;
import com.staybook.api.repository.RoomRepository;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.entity.RoomStatus;
import com.staybook.api.dto.reservation.CreateEventReservationRequest;
import com.staybook.api.dto.reservation.CreateHotelReservationRequest;
import com.staybook.api.entity.Event;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.repository.EventRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    public Reservation createHotelReservation(
        CreateHotelReservationRequest request
    ) {

        if (request.checkIn() == null || request.checkOut() == null) {
            throw new BusinessRuleException(
                    "Check-in and check-out dates are required."
            );
        }

        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new BusinessRuleException(
                    "Check-out date must be after check-in date."
            );
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.userId()));

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: " + request.roomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BusinessRuleException(
                    "Room is not available for reservation."
            );
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .type(ReservationType.HOTEL)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .status(ReservationStatus.CONFIRMED)
                .build();

        return reservationRepository.save(reservation);
    }

    public Reservation createEventReservation(
        CreateEventReservationRequest request
    ) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.userId()));

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found with id: " + request.eventId()));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot reserve a cancelled event."
            );
        }

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Cannot reserve a completed event."
            );
        }

        if (event.getAvailableSeats() <= 0) {
            throw new BusinessRuleException(
                    "Event has no available seats."
            );
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .build();

        return reservationRepository.save(reservation);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id));
    }

    public List<Reservation> getReservationsByUser(Long userId) {
         return reservationRepository.findByUserId(userId);
    }

    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Reservation is already cancelled."
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return reservationRepository.save(reservation);
    }
}
