package com.cashly.cashly_api.warehouses.application.services;

import com.cashly.cashly_api.warehouses.application.ports.in.WarehouseDirectoryQuery;
import com.cashly.cashly_api.warehouses.application.ports.in.WarehouseDirectoryEntry;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import java.util.Optional;
import java.util.UUID;

public class RepositoryWarehouseDirectoryQuery implements WarehouseDirectoryQuery {
    private final WarehouseRepository warehouses;

    public RepositoryWarehouseDirectoryQuery(WarehouseRepository warehouses) {
        this.warehouses = warehouses;
    }

    @Override
    public Optional<WarehouseDirectoryEntry> findById(UUID warehouseId) {
        return warehouses.findById(warehouseId).map(warehouse ->
                new WarehouseDirectoryEntry(warehouse.getId(), warehouse.getName(), warehouse.getAddress()));
    }
}
