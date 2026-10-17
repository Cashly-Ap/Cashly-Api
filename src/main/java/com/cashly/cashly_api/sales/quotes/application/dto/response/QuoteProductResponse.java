package com.cashly.cashly_api.sales.quotes.application.dto.response;

import java.math.BigDecimal;

public record QuoteProductResponse(
        String id,
        String code,
        String name,
        String categoryId,
        String categoryName,
        boolean composite,
        boolean active,
        String photo,
        BigDecimal customerPrice
) {}
