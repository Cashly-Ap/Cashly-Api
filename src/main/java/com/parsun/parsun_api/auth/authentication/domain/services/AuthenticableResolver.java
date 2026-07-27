package com.parsun.parsun_api.auth.authentication.domain.services;

import java.util.Optional;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;

public interface AuthenticableResolver {

    Optional<Authenticable> resolveByEmail(String email);

    Optional<Authenticable> resolveByRif(Rif rif);
}
