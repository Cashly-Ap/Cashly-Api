package com.parsun.parsun_api.reports.stock.application.dto.response;

public record StockReportResponse(
        String warehouseName,
        String productName,
        String sku,
        String categoryName,
        int quantity,
        String status) {}
