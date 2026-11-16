package com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.model.WarehouseSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseDirectoryGateway {
    Optional<WarehouseSnapshot> findById(UUID warehouseId);
    WarehouseSnapshot requireById(UUID warehouseId);
}
