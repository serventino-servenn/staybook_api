package com.staybook.api.security;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.ReservationRepository;
import com.staybook.api.repository.UserRepository;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;

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
                Role.CUSTOMER
        );

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
                Role.ADMIN
        );

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
                Role.CUSTOMER
        );

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
                Role.ADMIN
        );

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
                Role.ADMIN
        );

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
                                "Bearer " + adminToken
                        )
        )
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
                Role.CUSTOMER
        );

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
                Role.ADMIN
        );

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
    void customerCanGetOwnReservation() throws Exception{

        String email = "customer-reservation@example.com";
        String token = createTestUserAndGetToken(
                email,
                Role.CUSTOMER
        );

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
                                "Bearer " + token
                        )
        )
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
                Role.CUSTOMER
        );

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
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(reservation.getId()))
        .andExpect(jsonPath("$.userId")
                .value(user.getId()))
        .andExpect(jsonPath("$.status")
                .value("CANCELLED"));
    }



    @Test
    void customerCannotGetAnotherCustomersReservation() throws Exception{

        String johnEmail = "john-reservation@example.com";
        String janeEmail = "jane-reservation@example.com";

        String johnToken = createTestUserAndGetToken(
                johnEmail,
                Role.CUSTOMER
        );

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
                                "Bearer " + johnToken
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
     void customerCannotCancelAnotherCustomersReservation() throws Exception {

        String customerEmail = "customer-cancel-other@example.com";
        String ownerEmail = "reservation-owner-cancel@example.com";

        String token = createTestUserAndGetToken(
                customerEmail,
                Role.CUSTOMER
        );

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
                                "Bearer " + token
                        )
        )
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
                Role.CUSTOMER
        );

        String adminToken = createTestUserAndGetToken(
                adminEmail,
                Role.ADMIN
        );

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
                                "Bearer " + adminToken
                        )
        )
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
                get("/api/reservations/" + reservation.getId())
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsNotFoundWhenReservationDoesNotExist() throws Exception {

        String token = createTestUserAndGetToken(
                "missing-reservation@example.com",
                Role.CUSTOMER
        );

        mockMvc.perform(
                get("/api/reservations/999999")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
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
                patch("/api/reservations/" + reservation.getId() + "/cancel")
        )
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
                Role.CUSTOMER
        );

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
                                "Bearer " + token
                        )
        )
        .andExpect(status().isBadRequest());

        Reservation unchangedReservation = reservationRepository
                .findById(reservation.getId())
                .orElseThrow();

        assertThat(unchangedReservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
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

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build();

        return jwtService.generateToken(userDetails);
    }
}
