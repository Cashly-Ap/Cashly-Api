package com.cashly.cashly_api.products.application.api;

import java.util.Optional;
import java.util.UUID;

public interface StockCatalogQuery {
    Optional<StockCatalogEntry> findProduct(UUID productId);
}
