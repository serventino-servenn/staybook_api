package com.staybook.api.security;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.UserRepository;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;

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

    private String createTestUserAndGetToken(String email, Role role) {

        userRepository.findByEmail(email)
                .ifPresent(userRepository::delete);

        User user = User.builder()
                .firstName("Test")
                .lastName(role.name())
                .email(email)
                .password("test-password")
                .role(role)
                .build();

        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        return jwtService.generateToken(userDetails);
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
}
