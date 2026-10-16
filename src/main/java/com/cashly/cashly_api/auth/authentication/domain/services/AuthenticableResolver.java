package com.cashly.cashly_api.auth.authentication.domain.services;

import java.util.Optional;

import com.cashly.cashly_api.auth.authentication.domain.Authenticable;

public interface AuthenticableResolver {

    Optional<Authenticable> resolveByEmail(String email);
}
