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
import com.staybook.api.dto.auth.LoginRequest;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

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

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest(
                        "test@example.com",
                        "password123"
                );

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("encoded-password")
                        .roles("CUSTOMER")
                        .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("test-jwt-token");

        String token = authService.login(request);

        assertThat(token).isEqualTo("test-jwt-token");

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void shouldRejectInvalidLogin() {

        LoginRequest request =
                new LoginRequest(
                        "test@example.com",
                        "wrong-password"
                );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(
                new BadCredentialsException("Invalid credentials")
        );

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));

        verifyNoInteractions(jwtService);
    }
}
