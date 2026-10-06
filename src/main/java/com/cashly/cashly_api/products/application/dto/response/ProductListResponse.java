package com.cashly.cashly_api.products.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductListResponse(
    String id,
    String code,
    String name,
    String categoryId,
    String categoryName,
    boolean composite,
    boolean active,
    BigDecimal purchasePrice,
    List<ProductPriceListItem> productPrices,
    boolean deletable
) {
    public record ProductPriceListItem(String priceTypeId, BigDecimal price) {}
}
