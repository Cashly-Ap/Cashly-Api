package com.cashly.cashly_api.reports.stock.application.dto.query;

import java.util.UUID;

public record StockPdfQuery(
        UUID warehouseId,
        UUID categoryId,
        Boolean withStock,
        Boolean lowStock) {}
