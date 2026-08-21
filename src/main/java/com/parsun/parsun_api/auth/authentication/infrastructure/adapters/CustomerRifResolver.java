package com.parsun.parsun_api.auth.authentication.infrastructure.adapters;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;
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
