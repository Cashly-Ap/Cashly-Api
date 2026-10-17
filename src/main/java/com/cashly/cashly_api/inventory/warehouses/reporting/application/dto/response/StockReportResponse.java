package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response;

public record StockReportResponse(
        String warehouseName,
        String productName,
        String sku,
        String categoryName,
        int quantity,
        String status) {}
