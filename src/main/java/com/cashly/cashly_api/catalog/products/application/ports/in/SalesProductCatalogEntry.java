package com.cashly.cashly_api.catalog.products.application.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SalesProductCatalogEntry(UUID id, String code, String name,
        UUID categoryId, String categoryName, boolean composite, boolean active,
        String photo, BigDecimal customerPrice, int totalStock,
        List<Stock> stock) {
    public SalesProductCatalogEntry {
        stock = List.copyOf(stock);
    }

    public record Stock(UUID warehouseId, String warehouseCode,
            String warehouseName, int quantity, int reserved, int available) {
    }
}
