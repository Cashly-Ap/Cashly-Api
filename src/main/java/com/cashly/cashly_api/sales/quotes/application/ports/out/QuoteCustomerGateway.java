package com.cashly.cashly_api.sales.quotes.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface QuoteCustomerGateway {
    Optional<Customer> findById(UUID customerId);

    record Customer(UUID id, String legalName, String rif, UUID priceTypeId) {
    }
}
