package com.cashly.cashly_api.platform.auth.authentication.infrastructure.integration;

import java.time.LocalDateTime;
import java.util.Set;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

public class UserAuthenticableAdapter implements Authenticable {

    private final User user;

    public UserAuthenticableAdapter(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    @Override
    public String getSubjectId() {
        return user.getId().getValue().toString();
    }

    @Override
    public String getEmail() {
        return user.getEmail().getValue();
    }

    @Override
    public String getHashedPassword() {
        return user.getPassword().getHashedValue();
    }

    @Override
    public String getUserName() {
        return user.getUserName().getValue();
    }

    @Override
    public AuthenticableType getType() {
        return AuthenticableType.USER;
    }

    @Override
    public UserRole getRole() {
        return user.getRole();
    }

    @Override
    public boolean canAuthenticate() {
        return user.canAuthenticate();
    }

    @Override
    public boolean isActive() {
        return user.isActive();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return user.getCreatedAt();
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return user.getUpdatedAt();
    }

    @Override
    public Set<BusinessModule> getModulePermissions() {
        return user.getModulePermissions();
    }

    public User getUser() {
        return user;
    }
}
