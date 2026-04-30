package com.parsun.parsun_api.reports.stock.application.query;

import java.util.UUID;

public record StockPdfQuery(
        UUID warehouseId,
        UUID categoryId,
        Boolean withStock,
        Boolean lowStock) {}
