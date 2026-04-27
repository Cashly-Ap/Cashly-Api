package com.parsun.parsun_api.warehouses.domain.repositories;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseStockRepository {
    WarehouseStock save(WarehouseStock stock);
    Optional<WarehouseStock> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);
    List<WarehouseStock> findByWarehouseId(UUID warehouseId);
    PageResponse<WarehouseStock> findByWarehouseIdPaged(UUID warehouseId, int page, int size);
    boolean existsByProductId(UUID productId);
}
