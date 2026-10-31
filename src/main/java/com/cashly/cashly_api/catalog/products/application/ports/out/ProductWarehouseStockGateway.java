package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductWarehouseStockGateway {
    List<Stock> findByProductId(UUID productId);
    record Stock(UUID warehouseId, String warehouseCode, String warehouseName,
            boolean warehouseExists, int quantity, int reserved, int available,
            LocalDateTime lastMovementAt) {
    }
}
