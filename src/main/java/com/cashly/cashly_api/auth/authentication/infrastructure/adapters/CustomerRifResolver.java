package com.cashly.cashly_api.auth.authentication.infrastructure.adapters;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.Rif;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class CustomerRifResolver implements AuthenticableResolver {
    private final CustomerRepository customerRepository;

    public CustomerRifResolver(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<Authenticable> resolveByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<Authenticable> resolveByRif(Rif rif) {
        return customerRepository.findActivePortalCustomerByRif(rif)
            .map(CustomerAuthenticableAdapter::new);
    }
}
