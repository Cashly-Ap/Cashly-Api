package com.cashly.cashly_api.price_types.application.dto.response;

import com.cashly.cashly_api.price_types.domain.entities.PriceType;

import java.time.LocalDateTime;

public record PriceTypeResponse(
    String id,
    String name,
    String description,
    boolean active,
    boolean deletable,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PriceTypeResponse from(PriceType priceType) {
        return from(priceType, true);
    }

    public static PriceTypeResponse from(PriceType priceType, boolean deletable) {
        return new PriceTypeResponse(
            priceType.getId().toString(),
            priceType.getName(),
            priceType.getDescription(),
            priceType.isActive(),
            deletable,
            priceType.getCreatedAt(),
            priceType.getUpdatedAt()
        );
    }
}
