package com.cashly.cashly_api.catalog.products.application.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StockCatalogEntry(UUID id, String code, String name,
        boolean composite, List<Component> components) {
    public StockCatalogEntry {
        components = List.copyOf(components);
    }

    public record Component(UUID productId, BigDecimal quantity) {
    }
}
