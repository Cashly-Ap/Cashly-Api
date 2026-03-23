package com.parsun.parsun_api.auth.authentication.domain.services;

import java.util.Optional;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;

public interface AuthenticableResolver {

    Optional<Authenticable> resolveByEmail(String email);
}
