package com.cashly.cashly_api.customers.application.api;
import java.util.Optional;
import java.util.UUID;
public interface CustomerCreditContextQuery {
    Optional<CustomerCreditContextEntry> findById(UUID customerId);
}
