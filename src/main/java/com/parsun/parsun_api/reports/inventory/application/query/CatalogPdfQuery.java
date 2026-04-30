package com.parsun.parsun_api.reports.inventory.application.query;

import java.util.UUID;

public record CatalogPdfQuery(
        String search,
        UUID categoryId,
        UUID warehouseId,
        Boolean composite,
        Boolean withStock,
        Boolean lowStock
) {}
