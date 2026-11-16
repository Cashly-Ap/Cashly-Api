package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

import java.util.Set;
import java.util.UUID;

public interface AvailableStockProductsQuery {
    Set<UUID> availableProductIds(Set<UUID> warehouseIds);
}