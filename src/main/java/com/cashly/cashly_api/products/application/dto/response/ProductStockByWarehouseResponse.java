package com.cashly.cashly_api.products.application.dto.response;

import com.cashly.cashly_api.products.application.ports.out.ProductWarehouseStockGateway.Stock;

import java.time.LocalDateTime;

public record ProductStockByWarehouseResponse(
        String warehouseId,
        String warehouseName,
        int quantity,
        LocalDateTime lastMovementAt
) {
    public static ProductStockByWarehouseResponse from(Stock stock) {
        return new ProductStockByWarehouseResponse(
                stock.warehouseId().toString(),
                stock.warehouseName(),
                stock.quantity(),
                stock.lastMovementAt()
        );
    }
}
