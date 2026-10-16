package com.cashly.cashly_api.warehouses.stock.domain;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;

import java.util.UUID;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    PageResponse<StockMovement> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId,
                                                               int page, int size);
    PageResponse<StockMovement> findByWarehouseIdAndMovementType(UUID warehouseId,
                                                                  MovementType movementType,
                                                                  int page, int size);
    PageResponse<StockMovement> findByWarehouseIdAndProductIdAndMovementType(
            UUID warehouseId, UUID productId, MovementType movementType, int page, int size);
    PageResponse<StockMovement> findByWarehouseId(UUID warehouseId, int page, int size);
    void deleteByProductId(UUID productId);
}
