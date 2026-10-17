package com.cashly.cashly_api.partners.customers.credit.application.ports.in;
import java.util.Optional;
import java.util.UUID;
public interface CustomerCreditContextQuery {
    Optional<CustomerCreditContextEntry> findById(UUID customerId);
}
