package com.cashly.cashly_api.platform.auth.authentication.infrastructure.services;

import java.util.Optional;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.integration.UserAuthenticableAdapter;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;

public class ChainedAuthenticableResolver implements AuthenticableResolver {

    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    public ChainedAuthenticableResolver(UserRepository userRepository,com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access) {
        this.userRepository = userRepository;
        this.access=access;
    }

    @Override
    public Optional<Authenticable> resolveByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(new Email(normalizedEmail))
            .filter(user -> { try { access.resolve(user.getId().getValue());return true; } catch (org.springframework.security.access.AccessDeniedException ex) { return false; } })
            .map(UserAuthenticableAdapter::new);
    }

}
