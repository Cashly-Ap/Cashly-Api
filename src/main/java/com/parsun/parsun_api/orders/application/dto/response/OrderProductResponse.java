package com.parsun.parsun_api.orders.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderProductResponse(
        String id,
        String code,
        String name,
        String categoryId,
        String categoryName,
        boolean composite,
        boolean active,
        String photo,
        BigDecimal customerPrice,
        int totalStock,
        List<WarehouseStockItem> stock
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
