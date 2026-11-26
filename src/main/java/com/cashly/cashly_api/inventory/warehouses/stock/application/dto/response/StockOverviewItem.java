package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import java.math.BigDecimal;

public record StockOverviewItem(String id, String warehouseId,
        String warehouseName, String productId, String productCode,
        String productName, String categoryId, String categoryName,
        boolean composite, BigDecimal quantity, BigDecimal reserved,
        BigDecimal available) {
}
