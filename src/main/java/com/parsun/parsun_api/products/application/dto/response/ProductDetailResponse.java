package com.parsun.parsun_api.products.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
    String id,
    String code,
    String name,
    String description,
    String categoryId,
    String categoryName,
    boolean composite,
    boolean active,
    BigDecimal basePrice,
    BigDecimal purchasePrice,
    BigDecimal profitMargin,
    List<ComponentResponse> components,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record ComponentResponse(
        String id,
        String componentProductId,
        String componentProductCode,
        String componentProductName,
        int quantity
    ) {}
}
