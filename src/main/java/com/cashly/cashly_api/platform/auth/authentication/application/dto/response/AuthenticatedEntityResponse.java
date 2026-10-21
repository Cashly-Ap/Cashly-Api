package com.cashly.cashly_api.platform.auth.authentication.application.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

public record AuthenticatedEntityResponse(
    String id,
    String email,
    String userName,
    AuthenticableType type,
    UserRole role,
    boolean active,
    Set<BusinessModule> modulePermissions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String photoPath
) {
    public AuthenticatedEntityResponse(String id, String email, String userName, AuthenticableType type,
            UserRole role, boolean active, Set<BusinessModule> modulePermissions,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, email, userName, type, role, active, modulePermissions, createdAt, updatedAt, null);
    }

    public static AuthenticatedEntityResponse from(Authenticable authenticable) {
        return new AuthenticatedEntityResponse(
            authenticable.getSubjectId(),
            authenticable.getEmail(),
            authenticable.getUserName(),
            authenticable.getType(),
            authenticable.getRole(),
            authenticable.isActive(),
            authenticable.getModulePermissions(),
            authenticable.getCreatedAt(),
            authenticable.getUpdatedAt(),
            authenticable.getPhotoPath()
        );
    }
}
