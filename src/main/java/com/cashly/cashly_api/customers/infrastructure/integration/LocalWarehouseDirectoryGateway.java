package com.cashly.cashly_api.customers.infrastructure.integration;

import com.cashly.cashly_api.customers.application.contracts.WarehouseDirectoryGateway;
import com.cashly.cashly_api.customers.application.contracts.WarehouseSnapshot;
import com.cashly.cashly_api.warehouses.application.api.WarehouseDirectoryQuery;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalWarehouseDirectoryGateway implements WarehouseDirectoryGateway {
    private final WarehouseDirectoryQuery warehouses;

    public LocalWarehouseDirectoryGateway(WarehouseDirectoryQuery warehouses) {
        this.warehouses = warehouses;
    }

    @Override
    public Optional<WarehouseSnapshot> findById(UUID warehouseId) {
        return warehouses.findById(warehouseId).map(warehouse -> new WarehouseSnapshot(warehouse.id(), warehouse.name()));
    }

    @Override
    public WarehouseSnapshot requireById(UUID warehouseId) {
        return findById(warehouseId).orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
    }
}
