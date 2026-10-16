package com.cashly.cashly_api.warehouses.management.application.update;

import com.cashly.cashly_api.warehouses.management.application.update.UpdateWarehouseRequest;
import com.cashly.cashly_api.warehouses.management.application.dto.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.management.application.create.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    public UpdateWarehouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public WarehouseDetailResponse execute(UUID id, UpdateWarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));

        warehouse.update(
                request.name(), request.address(),
                request.state(), request.municipality(), request.postalCode(), request.active()
        );

        Warehouse saved = warehouseRepository.save(warehouse);
        return WarehouseDetailResponse.from(saved);
    }
}
