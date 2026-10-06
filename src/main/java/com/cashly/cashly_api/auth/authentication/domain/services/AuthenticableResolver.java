package com.cashly.cashly_api.auth.authentication.domain.services;

import java.util.Optional;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.customers.domain.valueobjects.Rif;

public interface AuthenticableResolver {

    Optional<Authenticable> resolveByEmail(String email);

    Optional<Authenticable> resolveByRif(Rif rif);
}
