package com.cashly.cashly_api.products.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface StockCatalogQuery {
    Optional<StockCatalogEntry> findProduct(UUID productId);
}
