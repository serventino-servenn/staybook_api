package com.staybook.api.dto.auth;

import java.time.LocalDateTime;

import com.staybook.api.entity.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
}
