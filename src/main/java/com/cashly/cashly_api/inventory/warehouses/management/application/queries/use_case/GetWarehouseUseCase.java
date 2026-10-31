package com.cashly.cashly_api.inventory.warehouses.management.application.queries.use_case;

import com.cashly.cashly_api.inventory.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;

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
        boolean deletable = !warehouseRepository
                .findIdsWithActiveStock(Set.of(id)).contains(id);
        return WarehouseDetailResponse.from(warehouse, deletable);
    }
}
