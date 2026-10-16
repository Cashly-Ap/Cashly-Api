package com.cashly.cashly_api.customers.profile.application.queries;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerProductPricingQuery;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import java.util.Optional;
import java.util.UUID;

public class RepositoryCustomerProductPricingQuery implements CustomerProductPricingQuery {
    private final CustomerRepository customers;

    public RepositoryCustomerProductPricingQuery(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public Optional<Context> findById(UUID customerId) {
        return customers.findById(new CustomerId(customerId)).map(customer ->
                new Context(customerId, customer.getPriceTypeId()));
    }
}
