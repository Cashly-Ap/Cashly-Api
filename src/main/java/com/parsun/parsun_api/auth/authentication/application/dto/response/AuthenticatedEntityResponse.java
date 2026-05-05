package com.parsun.parsun_api.auth.authentication.application.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

public record AuthenticatedEntityResponse(
    String id,
    String email,
    String userName,
    AuthenticableType type,
    UserRole role,
    boolean active,
    Set<BusinessModule> modulePermissions,
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
            authenticable.getModulePermissions(),
            authenticable.getCreatedAt(),
            authenticable.getUpdatedAt()
        );
    }
}
