package com.cashly.cashly_api.partners.customers.profile.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileQuery {
    CustomerProfileSnapshot find(UUID customerId);
    Optional<CustomerProfileSnapshot> findOptional(UUID customerId);
}
