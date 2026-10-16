package com.cashly.cashly_api.warehouses.management.application.queries;

import com.cashly.cashly_api.warehouses.management.application.dto.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;

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
