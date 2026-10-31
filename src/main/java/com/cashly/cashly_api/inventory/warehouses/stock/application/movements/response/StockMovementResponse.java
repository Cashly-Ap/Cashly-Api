package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;

import java.time.LocalDateTime;

public record StockMovementResponse(String id, String warehouseId,
        String warehouseName, String productId, String productCode,
        String productName, String categoryName, MovementType movementType,
        int quantity, int previousStock, int newStock, String originMovementId,
        String transferBatchId, ReferenceType referenceType, String referenceId,
        String referenceNumber, String notes, LocalDateTime createdAt) {
    public static StockMovementResponse from(StockMovement m, String productCode,
            String productName) {
        return from(m, null, productCode, productName, null);
    }

    public static StockMovementResponse from(StockMovement m,
            String warehouseName, String productCode, String productName) {
        return from(m, warehouseName, productCode, productName, null);
    }

    public static StockMovementResponse from(StockMovement m,
            String warehouseName, String productCode, String productName,
            String categoryName) {
        return new StockMovementResponse(
                m.getId() != null ? m.getId().toString() : null,
                m.getWarehouseId().toString(), warehouseName,
                m.getProductId().toString(), productCode, productName,
                categoryName, m.getMovementType(), m.getQuantity(),
                m.getPreviousStock(), m.getNewStock(),
                m.getOriginMovementId() != null
                        ? m.getOriginMovementId().toString()
                        : null,
                m.getTransferBatchId(), m.getReferenceType(),
                m.getReferenceId() != null
                        ? m.getReferenceId().toString()
                        : null,
                m.getReferenceNumber(), m.getNotes(), m.getCreatedAt());
    }
}
