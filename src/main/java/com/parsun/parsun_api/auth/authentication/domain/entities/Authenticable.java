package com.parsun.parsun_api.auth.authentication.domain.entities;

import java.time.LocalDateTime;

import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

public interface Authenticable {

    String getSubjectId();

    String getEmail();

    String getHashedPassword();

    String getUserName();

    AuthenticableType getType();

    boolean canAuthenticate();

    boolean isActive();

    LocalDateTime getCreatedAt();

    default LocalDateTime getUpdatedAt() {
        return null;
    }

    default UserRole getRole() {
        return null;
    }
}
