package com.cashly.cashly_api.reports.inventory.application.dto.query;

import java.util.UUID;

public record CatalogPdfQuery(
        String search,
        UUID categoryId,
        UUID warehouseId,
        Boolean composite,
        Boolean withStock,
        Boolean lowStock
) {}
