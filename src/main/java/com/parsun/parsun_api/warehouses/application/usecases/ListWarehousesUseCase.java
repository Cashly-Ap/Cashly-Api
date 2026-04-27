package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseListResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

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

        return new PageResponse<>(
                result.content().stream().map(WarehouseListResponse::from).toList(),
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
