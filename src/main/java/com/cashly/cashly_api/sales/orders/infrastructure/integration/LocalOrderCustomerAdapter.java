package com.cashly.cashly_api.sales.orders.infrastructure.integration;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerProfileQuery;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderCustomerGateway;
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
