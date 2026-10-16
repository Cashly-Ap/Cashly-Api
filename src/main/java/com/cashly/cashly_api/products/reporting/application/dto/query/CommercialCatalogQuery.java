package com.cashly.cashly_api.products.reporting.application.dto.query;

import java.util.UUID;

public record CommercialCatalogQuery(
        String search,
        UUID categoryId,
        UUID priceTypeId,
        Boolean active
) {}
