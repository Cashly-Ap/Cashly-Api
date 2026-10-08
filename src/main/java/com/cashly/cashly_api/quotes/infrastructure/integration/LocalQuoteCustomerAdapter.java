package com.cashly.cashly_api.quotes.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerProfileQuery;
import com.cashly.cashly_api.quotes.application.contracts.QuoteCustomerGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LocalQuoteCustomerAdapter implements QuoteCustomerGateway {
    private final CustomerProfileQuery customers;

    public LocalQuoteCustomerAdapter(CustomerProfileQuery customers) {
        this.customers = customers;
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        var profile = customers.find(customerId);
        return profile == null ? Optional.empty()
                : Optional.of(new Customer(profile.id(), profile.legalName(), profile.rif(), profile.priceTypeId()));
    }
}
