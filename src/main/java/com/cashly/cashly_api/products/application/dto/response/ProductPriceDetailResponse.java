package com.cashly.cashly_api.products.application.dto.response;

import java.math.BigDecimal;

public record ProductPriceDetailResponse(
    String id,
    String priceTypeId,
    String priceTypeName,
    BigDecimal price,
    BigDecimal profitMargin
) {}
