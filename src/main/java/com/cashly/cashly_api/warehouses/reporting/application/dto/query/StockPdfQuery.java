package com.cashly.cashly_api.warehouses.reporting.application.dto.query;

import java.util.UUID;

public record StockPdfQuery(
        UUID warehouseId,
        UUID categoryId,
        Boolean withStock,
        Boolean lowStock) {}
