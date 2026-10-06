package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseHasActiveStockException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
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
