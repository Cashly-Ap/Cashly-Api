package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model;

import java.util.UUID;

public record StockPdfQuery(
        UUID warehouseId,
        UUID categoryId,
        Boolean withStock,
        Boolean lowStock) {}
