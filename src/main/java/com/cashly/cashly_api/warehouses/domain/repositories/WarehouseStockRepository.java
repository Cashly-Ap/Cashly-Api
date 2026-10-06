package com.cashly.cashly_api.warehouses.domain.repositories;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface WarehouseStockRepository {
    WarehouseStock save(WarehouseStock stock);
    Optional<WarehouseStock> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);
    Optional<WarehouseStock> findByWarehouseIdAndProductIdForUpdate(UUID warehouseId, UUID productId);
    List<WarehouseStock> findByWarehouseId(UUID warehouseId);
    List<WarehouseStock> findByProductId(UUID productId);
    List<WarehouseStock> findAllByProductIdIn(Set<UUID> productIds);
    PageResponse<WarehouseStock> findByWarehouseIdPaged(UUID warehouseId, int page, int size);
    boolean existsByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}
