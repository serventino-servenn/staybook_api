package com.staybook.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.staybook.api.dto.auth.CreateUserRequest;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(CreateUserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessRuleException(
                    "User already exists with email: " + request.email()
            );
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
