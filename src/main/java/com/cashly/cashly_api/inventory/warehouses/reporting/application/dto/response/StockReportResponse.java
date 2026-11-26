package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

import java.math.BigDecimal;

public record StockReportResponse(String warehouseName, String productName,
        String sku, String categoryName, BigDecimal quantity, String status) {
}
