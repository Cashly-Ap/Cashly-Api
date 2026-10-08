package com.cashly.cashly_api.products.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryGateway {
    Optional<Entry> findById(UUID id);
    Entry require(UUID id);
    record Entry(UUID id, String name) {}
}
