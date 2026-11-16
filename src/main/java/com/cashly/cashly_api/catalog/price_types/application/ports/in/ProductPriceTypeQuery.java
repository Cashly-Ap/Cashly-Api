package com.cashly.cashly_api.catalog.price_types.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface ProductPriceTypeQuery {
    Optional<Entry> findById(UUID id);
    record Entry(UUID id, String name) {
    }
}
