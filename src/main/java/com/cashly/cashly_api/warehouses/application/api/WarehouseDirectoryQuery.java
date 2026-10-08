package com.cashly.cashly_api.warehouses.application.api;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseDirectoryQuery {
    Optional<WarehouseDirectoryEntry> findById(UUID warehouseId);
}
