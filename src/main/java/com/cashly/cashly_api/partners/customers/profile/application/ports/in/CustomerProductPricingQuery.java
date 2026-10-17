package com.cashly.cashly_api.partners.customers.profile.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProductPricingQuery {
    Optional<Context> findById(UUID customerId);
    record Context(UUID customerId, UUID priceTypeId) {}
}
