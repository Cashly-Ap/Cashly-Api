package com.cashly.cashly_api.catalog.products.reporting.application.dto.query.model;

import java.util.UUID;

public record CommercialCatalogQuery(String search, UUID categoryId,
        UUID priceTypeId, Boolean active) {
}
