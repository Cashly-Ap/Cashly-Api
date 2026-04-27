package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

import java.util.UUID;

public class GetWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    public GetWarehouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseDetailResponse execute(UUID id) {
        return warehouseRepository.findById(id)
                .map(WarehouseDetailResponse::from)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }
}
