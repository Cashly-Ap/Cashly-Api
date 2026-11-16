package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.util.Set;
import java.util.UUID;

public interface ProductStockFilterGateway {
    Set<UUID> availableProductIds(Set<UUID> warehouseIds);
}