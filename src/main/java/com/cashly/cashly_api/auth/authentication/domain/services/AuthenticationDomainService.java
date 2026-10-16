package com.cashly.cashly_api.auth.authentication.domain.services;

import com.cashly.cashly_api.auth.user.domain.User;

public class AuthenticationDomainService {

    public boolean canUserAuthenticate(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return user.canAuthenticate();
    }

    public boolean canPerformSensitiveOperations(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return user.isActive() && user.isEmailVerified();
    }
}
