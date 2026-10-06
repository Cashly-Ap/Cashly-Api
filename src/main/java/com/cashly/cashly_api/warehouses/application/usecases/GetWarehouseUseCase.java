package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;

import java.util.UUID;
import java.util.Set;

public class GetWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    public GetWarehouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseDetailResponse execute(UUID id) {
        var warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        boolean deletable = !warehouseRepository.findIdsWithActiveStock(Set.of(id)).contains(id);
        return WarehouseDetailResponse.from(warehouse, deletable);
    }
}
