package com.parsun.parsun_api.auth.authentication.infrastructure.adapters;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.user.domain.entities.User;

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
    public String getDisplayName() {
        return user.getUserName().getValue();
    }

    @Override
    public AuthenticableType getType() {
        return AuthenticableType.USER;
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
    public BigDecimal getCommissionDeductionPercentage() {
        return user.getCommissionDeductionPercentage().value();
    }

    public User getUser() {
        return user;
    }
}
