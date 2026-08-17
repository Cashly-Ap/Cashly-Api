package com.parsun.parsun_api.warehouses.application.dto.response;

import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;

import java.time.LocalDateTime;

public record WarehouseStockResponse(
        String id,
        String warehouseId,
        String productId,
        String productCode,
        String productName,
        boolean composite,
        int quantity,
        LocalDateTime lastMovementAt,
        LocalDateTime updatedAt
) {
    public static WarehouseStockResponse from(WarehouseStock stock, String productCode,
                                               String productName, boolean composite) {
        return new WarehouseStockResponse(
                stock.getId() != null ? stock.getId().toString() : null,
                stock.getWarehouseId().toString(),
                stock.getProductId().toString(),
                productCode, productName, composite,
                stock.getQuantity(), stock.getLastMovementAt(), stock.getUpdatedAt()
        );
    }
}
