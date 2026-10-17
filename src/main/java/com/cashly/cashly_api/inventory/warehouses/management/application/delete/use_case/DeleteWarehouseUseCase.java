package com.cashly.cashly_api.inventory.warehouses.management.application.delete.use_case;

import com.cashly.cashly_api.inventory.warehouses.management.application.delete.services.WarehouseHasActiveStockException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
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
