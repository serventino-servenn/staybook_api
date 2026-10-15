package com.staybook.api.service;

import com.staybook.api.dto.reservation.CreateEventReservationRequest;
import com.staybook.api.dto.reservation.CreateHotelReservationRequest;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomStatus;
import com.staybook.api.entity.RoomType;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.repository.*;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;
import com.staybook.api.entity.Event;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.entity.ReservationType;

import java.time.LocalDateTime;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventRepository eventRepository;



    @Test
    void concurrentOverlappingHotelReservationsShouldAllowOnlyOneBooking()
            throws Exception {

        User firstUser = createUser();
        User secondUser = createUser();
        Hotel hotel = createHotel();
        Room room = createRoom(hotel);

        CreateHotelReservationRequest firstRequest =
                new CreateHotelReservationRequest(
                        room.getId(),
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 12)
                );

        CreateHotelReservationRequest secondRequest =
                new CreateHotelReservationRequest(
                        room.getId(),
                        LocalDate.of(2026, 11, 11),
                        LocalDate.of(2026, 11, 13)
                );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            Future<Boolean> first = executor.submit(() -> {
                startSignal.await();
                try {
                    reservationService.createHotelReservation(
                            firstRequest, firstUser.getEmail());
                    return true;
                } catch (BusinessRuleException exception) {
                    return false;
                }
            });

            Future<Boolean> second = executor.submit(() -> {
                startSignal.await();
                try {
                    reservationService.createHotelReservation(
                            secondRequest, secondUser.getEmail());
                    return true;
                } catch (BusinessRuleException exception) {
                    return false;
                }
            });

            startSignal.countDown();

            boolean firstSucceeded = first.get(10, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(10, TimeUnit.SECONDS);

            assertThat(firstSucceeded).isNotEqualTo(secondSucceeded);
        } finally {
            executor.shutdownNow();
        }

        long confirmedReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getRoom() != null)
                .filter(r -> r.getRoom().getId().equals(room.getId()))
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        assertThat(confirmedReservations).isEqualTo(1);
    }



    @Test
    void concurrentEventReservationsShouldAllowOnlyOneBookingForLastSeat()
            throws Exception {

        User firstUser = createUser();
        User secondUser = createUser();
        Event event = createEventWithOneAvailableSeat();

        CreateEventReservationRequest request =
                new CreateEventReservationRequest(event.getId());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            Future<Boolean> firstAttempt = executor.submit(() -> {
                startSignal.await();
                try {
                    reservationService.createEventReservation(
                            request, firstUser.getEmail());
                    return true;
                } catch (BusinessRuleException exception) {
                    return false;
                }
            });

            Future<Boolean> secondAttempt = executor.submit(() -> {
                startSignal.await();
                try {
                    reservationService.createEventReservation(
                            request, secondUser.getEmail());
                    return true;
                } catch (BusinessRuleException exception) {
                    return false;
                }
            });

            startSignal.countDown();

            boolean firstSucceeded = firstAttempt.get();
            boolean secondSucceeded = secondAttempt.get();

            assertThat(firstSucceeded).isNotEqualTo(secondSucceeded);

            Event updatedEvent = eventRepository.findById(event.getId())
                    .orElseThrow();

            assertThat(updatedEvent.getAvailableSeats()).isZero();


            assertThat(reservationRepository.findAll())
                    .filteredOn(reservation ->
                            reservation.getEvent() != null
                                    && reservation.getEvent().getId()
                                            .equals(event.getId())
                                    && reservation.getStatus()
                                            == ReservationStatus.CONFIRMED)
                    .hasSize(1);

        } finally {
            executor.shutdownNow();
        }
    }


    private User createUser() {
        return userRepository.save(User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("concurrency-" + System.nanoTime() + "@example.com")
                .password("password")
                .role(Role.CUSTOMER)
                .active(true)
                .build());
    }

    private Hotel createHotel() {
        return hotelRepository.save(Hotel.builder()
                .name("Concurrency Test Hotel")
                .address("123 Test Street")
                .city("Seattle")
                .country("USA")
                .active(true)
                .build());
    }

    private Room createRoom(Hotel hotel) {
        return roomRepository.save(Room.builder()
                .roomNumber("101-" + System.nanoTime())
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .hotel(hotel)
                .status(RoomStatus.AVAILABLE)
                .build());
    }

     private Event createEventWithOneAvailableSeat() {
        return eventRepository.save(Event.builder()
                .name("Concurrency Test Festival")
                .description("Event used for concurrency integration testing")
                .venue("Central Park")
                .eventDate(LocalDateTime.now().plusDays(30))
                .capacity(500)
                .availableSeats(1)
                .price(new BigDecimal("75.00"))
                .status(EventStatus.UPCOMING)
                .build());
    }


}