package com.staybook.api.dto.mapper;

import org.springframework.stereotype.Component;

import com.staybook.api.dto.auth.UserResponse;
import com.staybook.api.entity.User;

@Component
public class AuthMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
