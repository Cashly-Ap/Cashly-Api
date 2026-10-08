package com.cashly.cashly_api.orders.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerProfileQuery;
import com.cashly.cashly_api.orders.application.contracts.OrderCustomerGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalOrderCustomerAdapter implements OrderCustomerGateway {
    private final CustomerProfileQuery customers;

    public LocalOrderCustomerAdapter(CustomerProfileQuery customers) {
        this.customers = customers;
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        var profile = customers.find(customerId);
        return profile == null ? Optional.empty()
                : Optional.of(new Customer(profile.id(), profile.legalName(), profile.rif()));
    }
}
