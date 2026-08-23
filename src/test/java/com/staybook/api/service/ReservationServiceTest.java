package com.staybook.api.service;

import java.util.List;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomStatus;
import com.staybook.api.entity.RoomType;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.EventRepository;
import com.staybook.api.repository.ReservationRepository;
import com.staybook.api.repository.RoomRepository;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.entity.Event;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    

    // shouldGetReservationById()
    //shouldCancelReservation()
    //shouldThrowExceptionWhenCancellingAlreadyCancelledReservation()
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

        @Mock
    private EventRepository eventRepository;

    private ReservationService reservationService;



   @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                userRepository,
                roomRepository,
                eventRepository
        );
    }

    @Test
    void shouldCreateHotelReservation() {
        Long userId = 1L;
        Long roomId = 10L;
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .status(RoomStatus.AVAILABLE)
                .build();

       Reservation reservation = Reservation.builder()
        .user(user)
        .room(room)
        .type(ReservationType.HOTEL)
        .checkIn(checkIn)
        .checkOut(checkOut)
        .status(ReservationStatus.CONFIRMED)
        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .thenReturn(reservation);

        Reservation result = reservationService.createHotelReservation(userId, roomId, checkIn, checkOut);

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getType()).isEqualTo(ReservationType.HOTEL);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getRoom()).isEqualTo(room);

        verify(userRepository).findById(userId);
        verify(roomRepository).findById(roomId);
        verify(reservationRepository).save(org.mockito.ArgumentMatchers.any(Reservation.class));
        assertThat(result.getCheckIn()).isEqualTo(checkIn);
        assertThat(result.getCheckOut()).isEqualTo(checkOut);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 999L;
        Long roomId = 10L;
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(userId, roomId, checkIn, checkOut))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenRoomDoesNotExist() {
        Long userId = 1L;
        Long roomId = 999L;
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(userId, roomId, checkIn, checkOut))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found with id: 999");

        verify(userRepository).findById(userId);
        verify(roomRepository).findById(roomId);
    }

    @Test
    void shouldThrowExceptionWhenRoomIsNotAvailable() {
        Long userId = 1L;
        Long roomId = 10L;
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .status(RoomStatus.MAINTENANCE)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(userId, roomId, checkIn, checkOut))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Room is not available for reservation.");

        verify(userRepository).findById(userId);
        verify(roomRepository).findById(roomId);
    }

    @Test
    void shouldCreateEventReservation() {
        Long userId = 1L;
        Long eventId = 20L;

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Summer Music Festival")
                .venue("Downtown Arena")
                .capacity(500)
                .availableSeats(100)
                .status(EventStatus.UPCOMING)
                .build();

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        Reservation result =
                reservationService.createEventReservation(userId, eventId);

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getType()).isEqualTo(ReservationType.EVENT);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getRoom()).isNull();

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenEventIsCancelled() {
        Long userId = 1L;
        Long eventId = 20L;

        User user = User.builder()
                .id(userId)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Cancelled Festival")
                .venue("Downtown Arena")
                .availableSeats(100)
                .status(EventStatus.CANCELLED)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(userId, eventId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot reserve a cancelled event.");

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verifyNoInteractions(reservationRepository);
    }
    
    @Test
    void shouldThrowExceptionWhenEventHasNoAvailableSeats() {
        Long userId = 1L;
        Long eventId = 20L;

        User user = User.builder()
                .id(userId)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Sold Out Festival")
                .venue("Downtown Arena")
                .availableSeats(0)
                .status(EventStatus.UPCOMING)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(userId, eventId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event has no available seats.");

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldGetReservationById() {
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(reservationId);

        assertThat(result).isEqualTo(reservation);

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void shouldThrowExceptionWhenReservationDoesNotExist() {
        Long reservationId = 999L;

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.getReservationById(reservationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation not found with id: 999");

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void shouldGetReservationsByUser() {
        Long userId = 1L;

        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findByUserId(userId))
                .thenReturn(java.util.List.of(reservation1, reservation2));

        List<Reservation> result =
        reservationService.getReservationsByUser(userId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(reservation1, reservation2);

        verify(reservationRepository).findByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenReservationIsAlreadyCancelled() {
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CANCELLED)
                .build();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Reservation is already cancelled.");

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void shouldCancelReservation() {
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        Reservation result = reservationService.cancelReservation(reservationId);

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldThrowExceptionWhenCancellingReservationDoesNotExist() {
        Long reservationId = 999L;

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation not found with id: 999");

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenEventIsCompleted() {
        Long userId = 1L;
        Long eventId = 20L;

        User user = User.builder()
                .id(userId)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Completed Festival")
                .venue("Downtown Arena")
                .availableSeats(100)
                .status(EventStatus.COMPLETED)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(userId, eventId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot reserve a completed event.");

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowExceptionWhenReservationDatesAreMissing() {
        Long userId = 1L;
        Long roomId = 10L;

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(
                        userId,
                        roomId,
                        null,
                        null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Check-in and check-out dates are required.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowExceptionWhenCheckOutIsBeforeOrEqualToCheckIn() {
        Long userId = 1L;
        Long roomId = 10L;

        LocalDate checkIn = LocalDate.of(2026, 9, 12);
        LocalDate checkOut = LocalDate.of(2026, 9, 10);

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(
                        userId,
                        roomId,
                        checkIn,
                        checkOut))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Check-out date must be after check-in date.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowExceptionWhenEventDoesNotExist() {
        Long userId = 1L;
        Long eventId = 999L;

        User user = User.builder()
                .id(userId)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createEventReservation(userId, eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 999");

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verifyNoInteractions(reservationRepository);
    }


    
}