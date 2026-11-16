package com.cashly.cashly_api.platform.auth.authorization.infrastructure.security;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextCurrentActor implements CurrentActor {
    @Override
    public UUID requireId() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }
        return UUID.fromString(authentication.getName());
    }
}
