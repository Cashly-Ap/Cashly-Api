package com.cashly.cashly_api.reports.inventory.application.dto.query;

import java.util.UUID;

public record CommercialCatalogQuery(
        String search,
        UUID categoryId,
        UUID priceTypeId,
        Boolean active
) {}
