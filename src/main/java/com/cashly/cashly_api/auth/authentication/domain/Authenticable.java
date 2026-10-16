package com.cashly.cashly_api.auth.authentication.domain;

import java.time.LocalDateTime;
import java.util.Set;

import com.cashly.cashly_api.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;

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

    default Set<BusinessModule> getModulePermissions() {
        return Set.of();
    }
}
