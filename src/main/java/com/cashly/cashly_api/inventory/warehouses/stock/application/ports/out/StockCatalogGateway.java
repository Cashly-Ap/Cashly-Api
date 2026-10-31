package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockCatalogGateway {
    CatalogProduct getProduct(UUID productId);
    Optional<CatalogProduct> findProduct(UUID productId);

    record CatalogProduct(UUID id, String code, String name, boolean composite,
            List<Component> components) {
        public CatalogProduct {
            components = List.copyOf(components);
        }
    }

    record Component(UUID productId, int quantity) {
    }
}
