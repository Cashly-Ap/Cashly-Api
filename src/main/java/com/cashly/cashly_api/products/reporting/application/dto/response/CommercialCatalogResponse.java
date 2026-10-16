package com.cashly.cashly_api.products.reporting.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CommercialCatalogResponse(
        UUID productId,
        String productName,
        String sku,
        String description,
        String photo,
        UUID categoryId,
        String categoryName,
        BigDecimal price,
        String priceTypeName
) {}
