package com.cashly.cashly_api.products.application.dto.response;

import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;

import java.time.LocalDateTime;

public record ProductStockByWarehouseResponse(
        String warehouseId,
        String warehouseName,
        int quantity,
        LocalDateTime lastMovementAt
) {
    public static ProductStockByWarehouseResponse from(WarehouseStock stock, String warehouseName) {
        return new ProductStockByWarehouseResponse(
                stock.getWarehouseId().toString(),
                warehouseName,
                stock.getQuantity(),
                stock.getLastMovementAt()
        );
    }
}
