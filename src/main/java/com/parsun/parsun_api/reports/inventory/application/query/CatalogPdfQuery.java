package com.parsun.parsun_api.reports.inventory.application.query;

public record CatalogPdfQuery(
        String search,
        Long categoryId,
        Long warehouseId,
        Boolean composite,
        Boolean withStock,
        Boolean lowStock
) {}
