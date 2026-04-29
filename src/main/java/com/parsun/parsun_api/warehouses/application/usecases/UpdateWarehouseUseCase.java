package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.warehouses.application.dto.request.UpdateWarehouseRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.DuplicateWarehouseCodeException;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
