package com.cashly.cashly_api.customers.profile.application.queries;

import com.cashly.cashly_api.customers.profile.application.ports.in.CustomerCreditTermsQuery;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import java.util.UUID;

public class RepositoryCustomerCreditTermsQuery implements CustomerCreditTermsQuery {
    private final CustomerRepository customers;

    public RepositoryCustomerCreditTermsQuery(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public int creditDays(UUID customerId) {
        return customers.findById(new CustomerId(customerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId))
                .getCreditDays().getValue();
    }
}
