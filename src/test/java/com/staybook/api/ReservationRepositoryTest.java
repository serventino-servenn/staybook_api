package com.staybook.api;

import com.staybook.api.entity.Event;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomType;
import com.staybook.api.entity.User;
import com.staybook.api.repository.EventRepository;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.ReservationRepository;
import com.staybook.api.repository.RoomRepository;
import com.staybook.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void shouldSaveHotelReservation() {
       
        Hotel hotel = Hotel.builder()
            .name("Grand Stay Hotel")
            .description("Test hotel")
            .address("123 Main Street")
            .city("Atlanta")
            .country("USA")
            .build();

        hotelRepository.save(hotel);

         User user = createUser();
        userRepository.save(user);

        Room room = createRoom(hotel);
        roomRepository.save(room);

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        var result = reservationRepository.findById(savedReservation.getId());

        assertThat(result).isPresent();

        Reservation saved = result.get();

        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getRoom().getId()).isEqualTo(room.getId());
        assertThat(saved.getEvent()).isNull();
        assertThat(saved.getType()).isEqualTo(ReservationType.HOTEL);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getReservedAt()).isNotNull();
    }

    @Test
    void shouldSaveEventReservation() {
        User user = createUser();
        userRepository.save(user);

        Event event = createEvent();
        eventRepository.save(event);

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        var result = reservationRepository.findById(savedReservation.getId());

        assertThat(result).isPresent();

        Reservation saved = result.get();

        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getEvent().getId()).isEqualTo(event.getId());
        assertThat(saved.getRoom()).isNull();
        assertThat(saved.getType()).isEqualTo(ReservationType.EVENT);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getReservedAt()).isNotNull();
    }

    private User createUser() {
        return User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john-" + System.nanoTime() + "@example.com")
                .password("password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();
    }

    private Room createRoom(Hotel hotel) {
        return Room.builder()
                .roomNumber("101-" + System.nanoTime())
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .hotel(hotel)
                .build();
    }

    private Event createEvent() {
        return Event.builder()
                .name("Summer Music Festival")
                .description("Annual outdoor music festival")
                .venue("Central Park")
                .eventDate(LocalDateTime.now().plusDays(30))
                .capacity(500)
                .availableSeats(500)
                .price(new BigDecimal("75.00"))
                .status(EventStatus.UPCOMING)
                .build();
    }
}