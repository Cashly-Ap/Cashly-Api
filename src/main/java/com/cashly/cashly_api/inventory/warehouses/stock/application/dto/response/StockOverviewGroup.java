package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StockOverviewGroup(String id, String name, String productCode,
        String categoryName, BigDecimal quantity, BigDecimal reserved,
        BigDecimal available, long productCount, long warehouseCount,
        List<StockOverviewItem> items) {
}
