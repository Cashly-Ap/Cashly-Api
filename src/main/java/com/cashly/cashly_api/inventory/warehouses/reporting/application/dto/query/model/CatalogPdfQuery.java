package com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model;

import java.util.UUID;

public record CatalogPdfQuery(String search, UUID categoryId, UUID warehouseId,
        Boolean composite, Boolean withStock, Boolean lowStock) {
}
