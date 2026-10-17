package com.cashly.cashly_api.inventory.warehouses.management.application.ports.in;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseDirectoryQuery {
    Optional<WarehouseDirectoryEntry> findById(UUID warehouseId);
}
