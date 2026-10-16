package com.cashly.cashly_api.warehouses.stock.application.movements;

import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;

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
        ReferenceType referenceType,
        String referenceId,
        String referenceNumber,
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
                m.getReferenceType(),
                m.getReferenceId() != null ? m.getReferenceId().toString() : null,
                m.getReferenceNumber(),
                m.getNotes(), m.getCreatedAt()
        );
    }
}
