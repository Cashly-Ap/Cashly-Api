package com.cashly.cashly_api.products.application.contracts;

import java.util.Set;
import java.util.UUID;

public interface ProductStockFilterGateway {
    Set<UUID> availableProductIds(Set<UUID> warehouseIds);
}