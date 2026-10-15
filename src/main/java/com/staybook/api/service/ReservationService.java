package com.staybook.api.service;

import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.exception.ForbiddenException;
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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Reservation createHotelReservation(
        CreateHotelReservationRequest request, String email
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

        User user = userRepository.findByEmail(email)
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "User not found with email: " + email));

        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: " + request.roomId()));


        if (room.getStatus() != RoomStatus.AVAILABLE) {
        throw new BusinessRuleException(
                "Room is not available for reservation."
        );
        }

        boolean hasOverlap =
                reservationRepository.existsOverlappingReservation(
                        request.roomId(),
                        ReservationType.HOTEL,
                        ReservationStatus.CANCELLED,
                        request.checkIn(),
                        request.checkOut()
                );

        if (hasOverlap) {
        throw new BusinessRuleException(
                "Room is already reserved for the selected dates."
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

    @Transactional
    public Reservation createEventReservation(
        CreateEventReservationRequest request,String email
    ) {

       User user = userRepository.findByEmail(email)
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "User not found with email: " + email));

        Event event = eventRepository.findByIdForUpdate(request.eventId())
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

        event.setAvailableSeats(event.getAvailableSeats() - 1);

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .build();

                return reservationRepository.save(reservation);
   }

   public Reservation getReservationById(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id));

        if (user.getRole() != Role.ADMIN
        && !reservation.getUser().getId().equals(user.getId())) {

                throw new ForbiddenException(
                        "You are not allowed to access this reservation."
                );
        }

        return reservation;
    }

    public List<Reservation> getReservationsByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email));

        return reservationRepository.findByUserId(user.getId());
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId,String email) {

        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
        .orElseThrow(() -> new ResourceNotFoundException(
                "Reservation not found with id: " + reservationId
        ));

        User currentUser = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with email: " + email
        ));

        if (currentUser.getRole() != Role.ADMIN
                && !reservation.getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException(
                "You are not allowed to access this reservation."
        );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Reservation is already cancelled."
            );
        }

        if (reservation.getType() == ReservationType.EVENT) {
                Event event = reservation.getEvent();
                event.setAvailableSeats(event.getAvailableSeats() + 1);
        }


        reservation.setStatus(ReservationStatus.CANCELLED);

        return reservationRepository.save(reservation);
    }
}
