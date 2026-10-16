package com.cashly.cashly_api.warehouses.reporting.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogReportResponse(
        UUID productId,
        String productName,
        String sku,
        UUID categoryId,
        String categoryName,
        Boolean composite,
        UUID warehouseId,
        String warehouseName,
        BigDecimal quantity,
        Boolean active
) {}
