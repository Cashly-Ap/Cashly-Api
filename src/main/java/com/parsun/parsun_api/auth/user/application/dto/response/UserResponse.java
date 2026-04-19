package com.parsun.parsun_api.auth.user.application.dto.response;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
    String id,
    String email,
    String userName,
    UserRole role,
    boolean active,
    boolean emailVerified,
    Set<BusinessModule> modulePermissions,
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
            user.getModulePermissions(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
