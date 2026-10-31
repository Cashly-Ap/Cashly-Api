package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.util.Optional;
import java.util.UUID;

public interface ProductPriceTypeGateway {
    Optional<Entry> findById(UUID id);
    Entry require(UUID id);
    record Entry(UUID id, String name) {
    }
}
