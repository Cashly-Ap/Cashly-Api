package com.cashly.cashly_api.customers.application.api;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProductPricingQuery {
    Optional<Context> findById(UUID customerId);
    record Context(UUID customerId, UUID priceTypeId) {}
}
