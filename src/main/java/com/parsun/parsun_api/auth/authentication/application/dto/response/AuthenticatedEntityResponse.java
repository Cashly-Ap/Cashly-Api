package com.parsun.parsun_api.auth.authentication.application.dto.response;

import java.time.LocalDateTime;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

public record AuthenticatedEntityResponse(
    String id,
    String email,
    String userName,
    AuthenticableType type,
    UserRole role,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AuthenticatedEntityResponse from(Authenticable authenticable) {
        return new AuthenticatedEntityResponse(
            authenticable.getSubjectId(),
            authenticable.getEmail(),
            authenticable.getUserName(),
            authenticable.getType(),
            authenticable.getRole(),
            authenticable.isActive(),
            authenticable.getCreatedAt(),
            authenticable.getUpdatedAt()
        );
    }
}
