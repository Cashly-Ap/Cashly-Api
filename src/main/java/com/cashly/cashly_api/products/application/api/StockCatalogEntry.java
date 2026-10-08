package com.cashly.cashly_api.products.application.api;

import java.util.List;
import java.util.UUID;

public record StockCatalogEntry(UUID id, String code, String name, boolean composite, List<Component> components) {
    public StockCatalogEntry {
        components = List.copyOf(components);
    }

    public record Component(UUID productId, int quantity) {}
}
