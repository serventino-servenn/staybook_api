package com.staybook.api.controller;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.staybook.api.dto.auth.CreateUserRequest;
import com.staybook.api.dto.auth.UserResponse;
import com.staybook.api.dto.mapper.AuthMapper;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.service.AuthService;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthMapper authMapper;

    @Test
    void shouldRegisterUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Smith",
                "john@example.com",
                "password123"
        );

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        UserResponse response = new UserResponse(
                1L,
                "John",
                "Smith",
                "john@example.com",
                Role.CUSTOMER,
                true,
                LocalDateTime.of(2026, 9, 21, 10, 0)
        );

        when(authService.register(request))
                .thenReturn(user);

        when(authMapper.toResponse(user))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(authService).register(request);
        verify(authMapper).toResponse(user);
    }

    @Test
    void shouldRejectRegistrationWhenRequiredFieldsAreMissing() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
                "",
                "",
                "",
                ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRejectRegistrationWhenEmailIsInvalid() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Smith",
                "not-an-email",
                "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRejectRegistrationWhenPasswordIsTooShort() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Smith",
                "john@example.com",
                "short"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
