package com.parsun.parsun_api.auth.authentication.domain.services;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import org.springframework.stereotype.Service;

@Service
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
