package com.staybook.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.staybook.api.dto.auth.CreateUserRequest;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterCustomer() {

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Smith",
                "john@example.com",
                "password123"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        User savedUser = User.builder()
                .id(1L)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password("hashed-password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = authService.register(request);

        assertThat(result).isEqualTo(savedUser);
        assertThat(result.getEmail()).isEqualTo(request.email());
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(result.isActive()).isTrue();

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Smith",
                "john@example.com",
                "password123"
        );

        User existingUser = User.builder()
                .id(1L)
                .email(request.email())
                .build();

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() ->
                authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User already exists with email: " + request.email());

        verify(userRepository).findByEmail(request.email());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}
