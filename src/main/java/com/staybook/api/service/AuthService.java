package com.staybook.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.staybook.api.dto.auth.CreateUserRequest;
import com.staybook.api.dto.auth.LoginRequest;
import com.staybook.api.entity.Role;
import com.staybook.api.entity.User;
import com.staybook.api.exception.BusinessRuleException;
import com.staybook.api.repository.UserRepository;
import com.staybook.api.security.JwtService;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    public String login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        return jwtService.generateToken(
                (UserDetails) authentication.getPrincipal()
        );
    }
}
