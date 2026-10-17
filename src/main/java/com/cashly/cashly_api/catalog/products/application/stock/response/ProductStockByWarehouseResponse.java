package com.cashly.cashly_api.catalog.products.application.stock.response;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductWarehouseStockGateway.Stock;

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
