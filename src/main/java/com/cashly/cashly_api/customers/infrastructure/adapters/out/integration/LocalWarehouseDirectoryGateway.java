package com.cashly.cashly_api.customers.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.customers.application.ports.out.WarehouseDirectoryGateway;
import com.cashly.cashly_api.customers.application.ports.out.model.WarehouseSnapshot;
import com.cashly.cashly_api.warehouses.application.ports.in.WarehouseDirectoryQuery;
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
