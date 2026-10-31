package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import java.util.List;

public record StockOverviewGroup(String id, String name, String productCode,
        String categoryName, long quantity, long reserved, long available,
        long productCount, long warehouseCount, List<StockOverviewItem> items) {
}
