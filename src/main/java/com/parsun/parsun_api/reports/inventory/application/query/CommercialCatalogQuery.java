package com.parsun.parsun_api.reports.inventory.application.query;

import java.util.UUID;

public record CommercialCatalogQuery(
        String search,
        UUID categoryId,
        UUID priceTypeId,
        Boolean active
) {}
