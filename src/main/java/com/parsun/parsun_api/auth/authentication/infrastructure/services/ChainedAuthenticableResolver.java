package com.parsun.parsun_api.auth.authentication.infrastructure.services;

import java.util.Optional;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.authentication.infrastructure.adapters.UserAuthenticableAdapter;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;

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
