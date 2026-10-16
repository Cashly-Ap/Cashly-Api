package com.cashly.cashly_api.auth.authentication.infrastructure.services;

import java.util.Optional;

import com.cashly.cashly_api.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.authentication.infrastructure.integration.UserAuthenticableAdapter;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;

public class ChainedAuthenticableResolver implements AuthenticableResolver {

    private final UserRepository userRepository;

    public ChainedAuthenticableResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Authenticable> resolveByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(new Email(normalizedEmail))
            .map(UserAuthenticableAdapter::new);
    }

}
