package com.cashly.cashly_api.platform.auth.authentication.application.dto.response;

import java.time.LocalDateTime;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

public record AuthenticatedEntityResponse(String id, String email,
        String userName, AuthenticableType type, UserRole role, boolean active,
        LocalDateTime createdAt, LocalDateTime updatedAt, String photoPath) {
    public AuthenticatedEntityResponse(String id, String email, String userName,
            AuthenticableType type, UserRole role, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, email, userName, type, role, active, createdAt, updatedAt,
                null);
    }

    public static AuthenticatedEntityResponse from(
            Authenticable authenticable) {
        return new AuthenticatedEntityResponse(authenticable.getSubjectId(),
                authenticable.getEmail(), authenticable.getUserName(),
                authenticable.getType(), authenticable.getRole(),
                authenticable.isActive(), authenticable.getCreatedAt(),
                authenticable.getUpdatedAt(), authenticable.getPhotoPath());
    }
}
