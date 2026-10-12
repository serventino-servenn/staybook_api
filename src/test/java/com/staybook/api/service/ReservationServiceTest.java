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
import com.staybook.api.exception.ForbiddenException;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.EventRepository;
import com.staybook.api.repository.ReservationRepository;
import com.staybook.api.repository.RoomRepository;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.dto.reservation.CreateEventReservationRequest;
import com.staybook.api.dto.reservation.CreateHotelReservationRequest;
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


@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

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
        CreateHotelReservationRequest request =
                new CreateHotelReservationRequest(
                        1L,
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 12)
                );

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Room room = Room.builder()
                .id(request.roomId())
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
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .status(ReservationStatus.CONFIRMED)
                .build();

       when(userRepository.findByEmail("john@example.com"))
        .thenReturn(Optional.of(user));

        when(roomRepository.findById(request.roomId()))
                .thenReturn(Optional.of(room));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        Reservation result =
        reservationService.createHotelReservation(
                request,
                "john@example.com"
        );

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getType()).isEqualTo(ReservationType.HOTEL);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getRoom()).isEqualTo(room);
        assertThat(result.getCheckIn()).isEqualTo(request.checkIn());
        assertThat(result.getCheckOut()).isEqualTo(request.checkOut());

        verify(userRepository).findByEmail("john@example.com");
        verify(roomRepository).findById(request.roomId());
        verify(reservationRepository).save(any(Reservation.class));
    }

     @Test
    void shouldCreateEventReservation() {
        CreateEventReservationRequest request =
        new CreateEventReservationRequest(1L);

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(request.eventId())
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

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(request.eventId()))
                .thenReturn(Optional.of(event));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        Reservation result =
                reservationService.createEventReservation(
                        request,
                        "john@example.com")
                        ;

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getType()).isEqualTo(ReservationType.EVENT);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getRoom()).isNull();

        verify(userRepository).findByEmail("john@example.com");
        verify(eventRepository).findById(request.eventId());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenRoomDoesNotExist() {
        CreateHotelReservationRequest request =
                new CreateHotelReservationRequest(
                        1L,
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12)
                );

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(roomRepository.findById(request.roomId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(request,"john@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found with id: 1");

        verify(userRepository).findByEmail("john@example.com");
        verify(roomRepository).findById(request.roomId());
    }

    @Test
    void shouldThrowExceptionWhenRoomIsNotAvailable() {
        CreateHotelReservationRequest request =
                new CreateHotelReservationRequest(
                        1L,
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12)
                );

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Room room = Room.builder()
                .id(request.roomId())
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .status(RoomStatus.MAINTENANCE)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(roomRepository.findById(request.roomId()))
                .thenReturn(Optional.of(room));

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(request,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Room is not available for reservation.");

        verify(userRepository).findByEmail("john@example.com");
        verify(roomRepository).findById(request.roomId());

        verify(reservationRepository, never())
            .save(any(Reservation.class));
    }


    @Test
    void shouldThrowExceptionWhenEventIsCancelled() {
        CreateEventReservationRequest request =
         new CreateEventReservationRequest(1L);

        User user = User.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(request.eventId())
                .name("Cancelled Festival")
                .venue("Downtown Arena")
                .availableSeats(100)
                .status(EventStatus.CANCELLED)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(request.eventId()))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(request,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot reserve a cancelled event.");

        verify(userRepository).findByEmail("john@example.com");
        verify(eventRepository).findById(request.eventId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenEventHasNoAvailableSeats() {
        CreateEventReservationRequest request = new CreateEventReservationRequest(1L);

        User user = User.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(request.eventId())
                .name("Sold Out Festival")
                .venue("Downtown Arena")
                .availableSeats(0)
                .status(EventStatus.UPCOMING)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(request.eventId()))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(request,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Event has no available seats.");

        verify(userRepository).findByEmail("john@example.com");
        verify(eventRepository).findById(request.eventId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldGetReservationById() {

        Long reservationId = 1L;

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(user)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(
                reservationId,
                "john@example.com"
        );

        assertThat(result).isEqualTo(reservation);

        verify(userRepository).findByEmail("john@example.com");
        verify(reservationRepository).findById(reservationId);
     }

     @Test
     void shouldThrowExceptionWhenCustomerDoesNotOwnReservation() {

        User customer = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User reservationOwner = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(reservationOwner)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(customer));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.getReservationById(
                        1L,
                        "john@example.com"
                ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You are not authorized to access this reservation.");

        verify(userRepository).findByEmail("john@example.com");
        verify(reservationRepository).findById(1L);
     }

     @Test
     void shouldThrowExceptionWhenCustomerDoesNotOwnReservationWhenCancelling() {

        User customer = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User reservationOwner = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(reservationOwner)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(customer));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.cancelReservation(
                        1L,
                        "john@example.com"
                ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You are not authorized to access this reservation.");

        verify(userRepository).findByEmail("john@example.com");
        verify(reservationRepository).findById(1L);
        verify(reservationRepository, never()).save(any(Reservation.class));
     }

    @Test
    void shouldThrowExceptionWhenReservationDoesNotExist() {
        Long reservationId = 999L;

        User user = User.builder()
        .id(1L)
        .firstName("John")
        .lastName("Smith")
        .email("john@example.com")
        .role(Role.CUSTOMER)
        .active(true)
        .build();

        when(userRepository.findByEmail("john@example.com"))
        .thenReturn(Optional.of(user));

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.getReservationById(reservationId,"john@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation not found with id: 999");

        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void shouldGetReservationsByUser() {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

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

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(reservation1, reservation2));

        List<Reservation> result =
                reservationService.getReservationsByUser("john@example.com");

        assertThat(result)
                .hasSize(2)
                .containsExactly(reservation1, reservation2);

        verify(userRepository).findByEmail("john@example.com");
        verify(reservationRepository).findByUserId(user.getId());
     }

    @Test
    void shouldThrowExceptionWhenReservationIsAlreadyCancelled() {
        Long reservationId = 1L;

        User user = User.builder()
        .id(1L)
        .firstName("John")
        .lastName("Smith")
        .email("john@example.com")
        .role(Role.CUSTOMER)
        .active(true)
        .build();


        when(userRepository.findByEmail("john@example.com"))
        .thenReturn(Optional.of(user));

        Reservation reservation = Reservation.builder()
        .id(1L)
        .user(user)
        .type(ReservationType.HOTEL)
        .status(ReservationStatus.CANCELLED)
        .build();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Reservation is already cancelled.");

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void shouldCancelReservation() {
        Long reservationId = 1L;

        User user = User.builder()
        .id(1L)
        .firstName("John")
        .lastName("Smith")
        .email("john@example.com")
        .role(Role.CUSTOMER)
        .active(true)
        .build();

        when(userRepository.findByEmail("john@example.com"))
        .thenReturn(Optional.of(user));

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(user)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        Reservation result =
              reservationService.cancelReservation(reservationId,"john@example.com");

        assertThat(result).isEqualTo(reservation);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldThrowExceptionWhenCancellingReservationDoesNotExist() {
        Long reservationId = 999L;

        User user = User.builder()
        .id(1L)
        .firstName("John")
        .lastName("Smith")
        .email("john@example.com")
        .role(Role.CUSTOMER)
        .active(true)
        .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.cancelReservation(
                        999L,
                        "john@example.com"
                ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation not found with id: 999");


        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void shouldThrowExceptionWhenEventIsCompleted() {
        CreateEventReservationRequest request = new CreateEventReservationRequest(1L);

        User user = User.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        Event event = Event.builder()
                .id(request.eventId())
                .name("Completed Festival")
                .venue("Downtown Arena")
                .availableSeats(100)
                .status(EventStatus.COMPLETED)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(request.eventId()))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                reservationService.createEventReservation(request,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot reserve a completed event.");

        verify(userRepository).findByEmail("john@example.com");
        verify(eventRepository).findById(request.eventId());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldThrowExceptionWhenReservationDatesAreMissing() {
        CreateHotelReservationRequest request =
                new CreateHotelReservationRequest(
                        10L,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                reservationService.createHotelReservation(
                        request,
                        "john@example.com"
                ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Check-in and check-out dates are required.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(reservationRepository);
     }

    @Test
    void shouldThrowExceptionWhenCheckOutIsBeforeOrEqualToCheckIn() {
        CreateHotelReservationRequest request =
        new CreateHotelReservationRequest(1L, LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 10));


        assertThatThrownBy(() ->
                reservationService.createHotelReservation(request,"john@example.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Check-out date must be after check-in date.");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(reservationRepository);


    }

    @Test
    void shouldThrowExceptionWhenEventDoesNotExist() {
        CreateEventReservationRequest request =
        new CreateEventReservationRequest(1L);

        User user = User.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(request.eventId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createEventReservation(request,"john@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 1");

        verify(userRepository).findByEmail("john@example.com");
        verify(eventRepository).findById(request.eventId());
        verifyNoInteractions(reservationRepository);
    }




}