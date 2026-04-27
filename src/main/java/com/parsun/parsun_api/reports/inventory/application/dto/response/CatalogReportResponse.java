package com.parsun.parsun_api.reports.inventory.application.dto.response;

import java.math.BigDecimal;

public record CatalogReportResponse(
        Long productId,
        String productName,
        String sku,
        Long categoryId,
        String categoryName,
        Boolean composite,
        Long warehouseId,
        String warehouseName,
        BigDecimal quantity,
        BigDecimal minStock,
        Boolean active
) {}
