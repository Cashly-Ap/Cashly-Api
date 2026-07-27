package com.parsun.parsun_api.portal.application.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PortalAuthorizationService {

    public UUID getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated customer found");
        }
        String principal = (String) authentication.getPrincipal();
        return UUID.fromString(principal);
    }
}
