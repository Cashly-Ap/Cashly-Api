package com.parsun.parsun_api.auth.user.application.dto.response;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String email,
    String userName,
    UserRole role,
    boolean active,
    boolean emailVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getEmail().getValue(),
            user.getUserName().getValue(),
            user.getRole(),
            user.isActive(),
            user.isEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
