package com.staybook.api.service;

import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void shouldGetUserById() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistById() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldGetUserByEmail() {
        String email = "john@example.com";

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email(email)
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistByEmail() {
        String email = "missing@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: missing@example.com");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User user2 = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .role(Role.ADMIN)
                .active(true)
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertThat(result)
                .hasSize(2)
                .containsExactly(user1, user2);

        verify(userRepository).findAll();
    }

    @Test
    void shouldDeactivateUser() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.deactivateUser(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(user.isActive()).isFalse();

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }
}