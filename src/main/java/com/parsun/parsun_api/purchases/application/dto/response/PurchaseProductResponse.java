package com.parsun.parsun_api.purchases.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseProductResponse(
        String id,
        String code,
        String name,
        String categoryId,
        String categoryName,
        boolean active,
        int stock,
        List<WarehouseStockItem> stockItems,
        BigDecimal purchasePrice
) {
    public record WarehouseStockItem(
            String warehouseId,
            String warehouseCode,
            String warehouseName,
            int quantity,
            int reserved,
            int available
    ) {}
}
