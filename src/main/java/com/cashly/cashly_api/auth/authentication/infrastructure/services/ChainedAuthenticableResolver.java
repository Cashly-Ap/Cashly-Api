package com.cashly.cashly_api.auth.authentication.infrastructure.services;

import java.util.Optional;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.authentication.infrastructure.adapters.UserAuthenticableAdapter;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.customers.domain.valueobjects.Rif;

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

    @Override
    public Optional<Authenticable> resolveByRif(Rif rif) {
        return Optional.empty();
    }
}
