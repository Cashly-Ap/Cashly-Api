package com.cashly.cashly_api.quotes.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface QuoteCustomerGateway {
    Optional<Customer> findById(UUID customerId);

    record Customer(UUID id, String legalName, String rif, UUID priceTypeId) {}
}
