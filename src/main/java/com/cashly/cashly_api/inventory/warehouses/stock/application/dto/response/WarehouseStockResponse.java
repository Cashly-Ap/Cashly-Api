package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WarehouseStockResponse(String id, String warehouseId,
        String productId, String productCode, String productName,
        boolean composite, BigDecimal quantity, LocalDateTime lastMovementAt,
        LocalDateTime updatedAt) {
    public static WarehouseStockResponse from(WarehouseStock stock,
            String productCode, String productName, boolean composite) {
        return new WarehouseStockResponse(
                stock.getId() != null ? stock.getId().toString() : null,
                stock.getWarehouseId().toString(),
                stock.getProductId().toString(), productCode, productName,
                composite, stock.getQuantity(), stock.getLastMovementAt(),
                stock.getUpdatedAt());
    }
}
