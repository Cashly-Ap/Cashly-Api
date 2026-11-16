package com.cashly.cashly_api.platform.auth.authentication.domain.services;

import java.util.Optional;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;

public interface AuthenticableResolver {

    Optional<Authenticable> resolveByEmail(String email);
}
