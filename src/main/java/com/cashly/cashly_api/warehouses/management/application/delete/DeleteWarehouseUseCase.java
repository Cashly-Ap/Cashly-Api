package com.cashly.cashly_api.warehouses.management.application.delete;

import com.cashly.cashly_api.warehouses.management.application.delete.WarehouseHasActiveStockException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    public DeleteWarehouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        if (warehouseRepository.hasActiveStock(id)) {
            throw new WarehouseHasActiveStockException(id);
        }
        warehouse.deactivate();
        warehouseRepository.save(warehouse);
    }
}
