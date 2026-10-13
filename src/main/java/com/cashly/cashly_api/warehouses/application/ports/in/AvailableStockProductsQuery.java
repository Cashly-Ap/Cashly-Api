package com.cashly.cashly_api.warehouses.application.ports.in;

import java.util.Set;
import java.util.UUID;

public interface AvailableStockProductsQuery {
    Set<UUID> availableProductIds(Set<UUID> warehouseIds);
}