package com.staybook.api.security;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
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

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/hotels"))
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
}
