package com.cashly.cashly_api.categories.application.api;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryQuery {
    Optional<Entry> findById(UUID id);
    record Entry(UUID id, String name) {}
}
