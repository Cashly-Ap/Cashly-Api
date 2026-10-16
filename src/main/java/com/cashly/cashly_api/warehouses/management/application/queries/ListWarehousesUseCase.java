package com.cashly.cashly_api.warehouses.management.application.queries;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.management.application.dto.WarehouseListResponse;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;

import java.util.Set;
import java.util.UUID;

public class ListWarehousesUseCase {

    private final WarehouseRepository warehouseRepository;

    public ListWarehousesUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public PageResponse<WarehouseListResponse> execute(String search, Boolean active,
                                                        int page, int size,
                                                        String orderBy, String orderDir) {
        PageResponse<Warehouse> result =
                warehouseRepository.findAllWithFilters(search, active, page, size, orderBy, orderDir);

        Set<UUID> idsWithStock = warehouseRepository.findIdsWithActiveStock(
            result.content().stream().map(Warehouse::getId).collect(java.util.stream.Collectors.toSet())
        );

        return new PageResponse<>(
                result.content().stream()
                    .map(w -> WarehouseListResponse.from(w, !idsWithStock.contains(w.getId())))
                    .toList(),
                result.totalElements(),
                result.totalPages(),
                result.size(),
                result.number(),
                result.first(),
                result.last(),
                result.empty()
        );
    }
}
