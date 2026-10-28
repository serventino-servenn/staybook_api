package com.staybook.api.security;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private HotelRepository hotelRepository;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private RoomRepository roomRepository;

        @Autowired
        private EventRepository eventRepository;

        @Value("${app.jwt.secret}")
        private String secret;

        @Test
        void shouldAllowPublicAccessToHotels() throws Exception {
                mockMvc.perform(get("/api/hotels"))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldAllowPublicAccessToEvents() throws Exception {
                mockMvc.perform(get("/api/events"))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldRejectCustomerFromCreatingRoom() throws Exception {

                String token = createTestUserAndGetToken(
                                "room-customer@test.com",
                                Role.CUSTOMER);

                mockMvc.perform(post("/api/rooms")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "hotelId": 1,
                                                    "roomNumber": "101",
                                                    "roomType": "SINGLE",
                                                    "pricePerNight": 100.00,
                                                    "capacity": 2
                                                }
                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldAllowAdminToCreateRoom() throws Exception {

                String token = createTestUserAndGetToken(
                                "room-admin@test.com",
                                Role.ADMIN);

                Hotel hotel = Hotel.builder()
                                .name("Room Test Hotel")
                                .description("Hotel for room security test")
                                .address("123 Test Street")
                                .city("Atlanta")
                                .country("USA")
                                .build();

                Hotel savedHotel = hotelRepository.save(hotel);

                mockMvc.perform(post("/api/rooms")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "hotelId": %d,
                                                    "roomNumber": "101",
                                                    "roomType": "SINGLE",
                                                    "pricePerNight": 100.00,
                                                    "capacity": 2
                                                }
                                                """.formatted(savedHotel.getId())))
                                .andExpect(status().isCreated());
        }

        @Test
        void shouldRejectHotelReservationWithoutToken() throws Exception {
                mockMvc.perform(post("/api/reservations/hotel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "userId": 1,
                                                    "roomId": 1,
                                                    "checkIn": "2026-10-10",
                                                    "checkOut": "2026-10-12"
                                                }
                                                """))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectCustomerFromCreatingHotel() throws Exception {

                String token = createTestUserAndGetToken(
                                "customer@test.com",
                                Role.CUSTOMER);

                mockMvc.perform(post("/api/hotels")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Test Hotel",
                                                    "description": "Test hotel description",
                                                    "address": "123 Test Street",
                                                    "city": "Atlanta",
                                                    "country": "USA"
                                                }
                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldAllowAdminToCreateHotel() throws Exception {

                String token = createTestUserAndGetToken(
                                "admin@test.com",
                                Role.ADMIN);

                mockMvc.perform(post("/api/hotels")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Test Admin Hotel",
                                                    "description": "Hotel created by an admin",
                                                    "address": "123 Test Street",
                                                    "city": "Atlanta",
                                                    "country": "USA"
                                                }
                                                """))
                                .andExpect(status().isCreated());
        }

        @Test
        void adminCanCancelAnyReservation() throws Exception {

                String adminEmail = "admin-cancel-reservation@example.com";
                String customerEmail = "customer-owned-reservation@example.com";

                String adminToken = createTestUserAndGetToken(
                                adminEmail,
                                Role.ADMIN);

                User customer = userRepository.findByEmail(customerEmail)
                                .orElseGet(() -> {
                                        User user = User.builder()
                                                        .firstName("Reservation")
                                                        .lastName("Customer")
                                                        .email(customerEmail)
                                                        .password("test-password")
                                                        .role(Role.CUSTOMER)
                                                        .active(true)
                                                        .build();

                                        return userRepository.save(user);
                                });

                Reservation reservation = Reservation.builder()
                                .user(customer)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                patch("/api/reservations/" + reservation.getId() + "/cancel")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id")
                                                .value(reservation.getId()))
                                .andExpect(jsonPath("$.status")
                                                .value("CANCELLED"));

                Reservation cancelledReservation = reservationRepository
                                .findById(reservation.getId())
                                .orElseThrow();

                assertThat(cancelledReservation.getStatus())
                                .isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        void shouldRejectCustomerFromCreatingEvent() throws Exception {

                String token = createTestUserAndGetToken(
                                "event-customer@test.com",
                                Role.CUSTOMER);

                mockMvc.perform(post("/api/events")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Customer Test Event",
                                                    "description": "Event creation security test",
                                                    "venue": "Test Venue",
                                                    "eventDate": "2026-12-15T19:00:00",
                                                    "capacity": 100,
                                                    "price": 50.00
                                                }
                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldAllowAdminToCreateEvent() throws Exception {

                String token = createTestUserAndGetToken(
                                "event-admin@test.com",
                                Role.ADMIN);

                mockMvc.perform(post("/api/events")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Admin Test Event",
                                                    "description": "Event created by admin",
                                                    "venue": "Test Venue",
                                                    "eventDate": "2026-12-15T19:00:00",
                                                    "capacity": 100,
                                                    "price": 50.00
                                                }
                                                """))
                                .andExpect(status().isCreated());
        }

        @Test
        void customerCanGetOwnReservation() throws Exception {

                String email = "customer-reservation@example.com";
                String token = createTestUserAndGetToken(
                                email,
                                Role.CUSTOMER);

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                Reservation reservation = Reservation.builder()
                                .user(user)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                get("/api/reservations/" + reservation.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id")
                                                .value(reservation.getId()))
                                .andExpect(jsonPath("$.userId")
                                                .value(user.getId()))
                                .andExpect(jsonPath("$.type")
                                                .value("HOTEL"))
                                .andExpect(jsonPath("$.status")
                                                .value("CONFIRMED"));
        }

        @Test
        void customerCanCancelOwnReservation() throws Exception {

                String email = "customer-cancel-reservation@example.com";
                String token = createTestUserAndGetToken(
                                email,
                                Role.CUSTOMER);

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                Reservation reservation = Reservation.builder()
                                .user(user)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                patch("/api/reservations/" + reservation.getId() + "/cancel")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id")
                                                .value(reservation.getId()))
                                .andExpect(jsonPath("$.userId")
                                                .value(user.getId()))
                                .andExpect(jsonPath("$.status")
                                                .value("CANCELLED"));
        }

        @Test
        void customerCannotGetAnotherCustomersReservation() throws Exception {

                String johnEmail = "john-reservation@example.com";
                String janeEmail = "jane-reservation@example.com";

                String johnToken = createTestUserAndGetToken(
                                johnEmail,
                                Role.CUSTOMER);

                User jane = userRepository.findByEmail(janeEmail)
                                .orElseGet(() -> {
                                        User user = User.builder()
                                                        .firstName("Jane")
                                                        .lastName("Customer")
                                                        .email(janeEmail)
                                                        .password("test-password")
                                                        .role(Role.CUSTOMER)
                                                        .active(true)
                                                        .build();

                                        return userRepository.save(user);
                                });

                Reservation reservation = Reservation.builder()
                                .user(jane)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                get("/api/reservations/" + reservation.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + johnToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void customerCannotCancelAnotherCustomersReservation() throws Exception {

                String customerEmail = "customer-cancel-other@example.com";
                String ownerEmail = "reservation-owner-cancel@example.com";

                String token = createTestUserAndGetToken(
                                customerEmail,
                                Role.CUSTOMER);

                User owner = userRepository.findByEmail(ownerEmail)
                                .orElseGet(() -> {
                                        User user = User.builder()
                                                        .email(ownerEmail)
                                                        .password("encoded-password")
                                                        .firstName("Reservation")
                                                        .lastName("Owner")
                                                        .role(Role.CUSTOMER)
                                                        .active(true)
                                                        .build();

                                        return userRepository.save(user);
                                });

                Reservation reservation = Reservation.builder()
                                .user(owner)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                patch("/api/reservations/" + reservation.getId() + "/cancel")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());

                Reservation unchangedReservation = reservationRepository
                                .findById(reservation.getId())
                                .orElseThrow();

                assertThat(unchangedReservation.getStatus())
                                .isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        void adminCanGetAnyReservation() throws Exception {

                String customerEmail = "reservation-owner@example.com";
                String adminEmail = "reservation-admin@example.com";

                createTestUserAndGetToken(
                                customerEmail,
                                Role.CUSTOMER);

                String adminToken = createTestUserAndGetToken(
                                adminEmail,
                                Role.ADMIN);

                User customer = userRepository.findByEmail(customerEmail)
                                .orElseThrow();

                Reservation reservation = Reservation.builder()
                                .user(customer)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                get("/api/reservations/" + reservation.getId())
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id")
                                                .value(reservation.getId()))
                                .andExpect(jsonPath("$.userId")
                                                .value(customer.getId()))
                                .andExpect(jsonPath("$.type")
                                                .value("HOTEL"))
                                .andExpect(jsonPath("$.status")
                                                .value("CONFIRMED"));
        }

        @Test
        void unauthenticatedUserCannotGetReservationById() throws Exception {

                String email = "unauthenticated-get-reservation@example.com";

                User user = userRepository.findByEmail(email)
                                .orElseGet(() -> {
                                        User newUser = User.builder()
                                                        .firstName("Test")
                                                        .lastName("Customer")
                                                        .email(email)
                                                        .password("test-password")
                                                        .role(Role.CUSTOMER)
                                                        .active(true)
                                                        .build();

                                        return userRepository.save(newUser);
                                });

                user = userRepository.save(user);

                Reservation reservation = Reservation.builder()
                                .user(user)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                get("/api/reservations/" + reservation.getId()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void returnsNotFoundWhenReservationDoesNotExist() throws Exception {

                String token = createTestUserAndGetToken(
                                "missing-reservation@example.com",
                                Role.CUSTOMER);

                mockMvc.perform(
                                get("/api/reservations/999999")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isNotFound());
        }

        @Test
        void unauthenticatedUserCannotCancelReservation() throws Exception {

                String email = "unauthenticated-cancel@example.com";

                User user = userRepository.findByEmail(email)
                                .orElseGet(() -> {
                                        User newUser = User.builder()
                                                        .firstName("Test")
                                                        .lastName("Customer")
                                                        .email(email)
                                                        .password("test-password")
                                                        .role(Role.CUSTOMER)
                                                        .active(true)
                                                        .build();

                                        return userRepository.save(newUser);
                                });

                Reservation reservation = Reservation.builder()
                                .user(user)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CONFIRMED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                patch("/api/reservations/" + reservation.getId() + "/cancel"))
                                .andExpect(status().isUnauthorized());

                Reservation unchangedReservation = reservationRepository
                                .findById(reservation.getId())
                                .orElseThrow();

                assertThat(unchangedReservation.getStatus())
                                .isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        void customerCannotCancelAlreadyCancelledReservation() throws Exception {

                String email = "already-cancelled-reservation@example.com";

                String token = createTestUserAndGetToken(
                                email,
                                Role.CUSTOMER);

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                Reservation reservation = Reservation.builder()
                                .user(user)
                                .type(ReservationType.HOTEL)
                                .status(ReservationStatus.CANCELLED)
                                .build();

                reservation = reservationRepository.save(reservation);

                mockMvc.perform(
                                patch("/api/reservations/" + reservation.getId() + "/cancel")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isBadRequest());

                Reservation unchangedReservation = reservationRepository
                                .findById(reservation.getId())
                                .orElseThrow();

                assertThat(unchangedReservation.getStatus())
                                .isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        void shouldRejectRequestWithInvalidJwt() throws Exception {
                mockMvc.perform(
                                get("/api/reservations")
                                                .header(
                                                                HttpHeaders.AUTHORIZATION,
                                                                "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectRequestWithExpiredJwt() throws Exception {
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername("expired-token@example.com")
                                .password("test-password")
                                .roles("CUSTOMER")
                                .build();

                String expiredToken = Jwts.builder()
                                .subject(userDetails.getUsername())
                                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                                .expiration(new Date(System.currentTimeMillis() - 60_000))
                                .signWith(getSigningKey())
                                .compact();

                mockMvc.perform(
                                get("/api/reservations")
                                                .header(
                                                                HttpHeaders.AUTHORIZATION,
                                                                "Bearer " + expiredToken))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void inactiveUserCannotAccessProtectedEndpointWithExistingToken()
                        throws Exception {
                String email = "deactivated-user@example.com";
                String token = createTestUserAndGetToken(email, Role.CUSTOMER);

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                user.setActive(false);
                userRepository.save(user);

                mockMvc.perform(
                                get("/api/reservations")
                                                .header(
                                                                HttpHeaders.AUTHORIZATION,
                                                                "Bearer " + token))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void customerCanCreateHotelReservation() throws Exception {
                String email = "hotel-reservation-integration@example.com";
                String token = createTestUserAndGetToken(email, Role.CUSTOMER);

                User user = userRepository.findByEmail(email).orElseThrow();

                Hotel hotel = hotelRepository.save(Hotel.builder()
                                .name("Integration Test Hotel")
                                .description("Hotel for integration testing")
                                .address("123 Test Street")
                                .city("Seattle")
                                .country("USA")
                                .build());

                Room room = roomRepository.save(Room.builder()
                                .roomNumber("INT-101")
                                .roomType(RoomType.SINGLE)
                                .pricePerNight(new BigDecimal("100.00"))
                                .capacity(1)
                                .hotel(hotel)
                                .build());

                LocalDate checkIn = LocalDate.now().plusDays(10);
                LocalDate checkOut = checkIn.plusDays(2);

                mockMvc.perform(post("/api/reservations/hotel")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                        "roomId": %d,
                                                        "checkIn": "%s",
                                                        "checkOut": "%s"
                                                }
                                                """.formatted(room.getId(), checkIn, checkOut)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId").value(user.getId()))
                                .andExpect(jsonPath("$.roomId").value(room.getId()))
                                .andExpect(jsonPath("$.type").value("HOTEL"))
                                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        void customerCanCreateEventReservation() throws Exception {
                String email = "event-reservation-integration@example.com";
                String token = createTestUserAndGetToken(email, Role.CUSTOMER);

                User user = userRepository.findByEmail(email).orElseThrow();

                Event event = eventRepository.save(Event.builder()
                                .name("Integration Test Event")
                                .description("Event for integration testing")
                                .venue("Seattle Convention Center")
                                .eventDate(LocalDateTime.now().plusDays(30))
                                .capacity(5)
                                .availableSeats(5)
                                .price(new BigDecimal("50.00"))
                                .status(EventStatus.UPCOMING)
                                .build());

                mockMvc.perform(post("/api/reservations/event")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                        "eventId": %d
                                                }
                                                """.formatted(event.getId())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId").value(user.getId()))
                                .andExpect(jsonPath("$.eventId").value(event.getId()))
                                .andExpect(jsonPath("$.type").value("EVENT"))
                                .andExpect(jsonPath("$.status").value("CONFIRMED"));

                Event updatedEvent = eventRepository.findById(event.getId())
                                .orElseThrow();

                assertThat(updatedEvent.getAvailableSeats()).isEqualTo(4);
        }

        private String createTestUserAndGetToken(String email, Role role) {

                User user = userRepository.findByEmail(email)
                                .orElseGet(() -> User.builder()
                                                .firstName("Test")
                                                .lastName(role.name())
                                                .email(email)
                                                .password("test-password")
                                                .role(role)
                                                .active(true)
                                                .build());

                user.setRole(role);
                user.setActive(true);

                user = userRepository.save(user);

                UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .roles(user.getRole().name())
                                .build();

                return jwtService.generateToken(userDetails);
        }

        private SecretKey getSigningKey() {
                return Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(secret));
        }
}
