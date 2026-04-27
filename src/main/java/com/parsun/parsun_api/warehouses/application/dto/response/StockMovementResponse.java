package com.parsun.parsun_api.warehouses.application.dto.response;

import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;

import java.time.LocalDateTime;

public record StockMovementResponse(
        String id,
        String warehouseId,
        String productId,
        String productCode,
        String productName,
        MovementType movementType,
        int quantity,
        int previousStock,
        int newStock,
        String originMovementId,
        String transferBatchId,
        String notes,
        LocalDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement m, String productCode, String productName) {
        return new StockMovementResponse(
                m.getId() != null ? m.getId().toString() : null,
                m.getWarehouseId().toString(),
                m.getProductId().toString(),
                productCode, productName,
                m.getMovementType(), m.getQuantity(),
                m.getPreviousStock(), m.getNewStock(),
                m.getOriginMovementId() != null ? m.getOriginMovementId().toString() : null,
                m.getTransferBatchId(),
                m.getNotes(), m.getCreatedAt()
        );
    }
}
