package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;

import java.math.BigDecimal;
import java.util.List;

public record StockMovementGroup(String id, String name,
        MovementType movementType, String productCode, String categoryName,
        String warehouseName, long movementCount, BigDecimal entryQuantity,
        BigDecimal exitQuantity, BigDecimal netQuantity,
        List<StockMovementResponse> items) {
    public StockMovementGroup {
        items = List.copyOf(items);
    }
}
