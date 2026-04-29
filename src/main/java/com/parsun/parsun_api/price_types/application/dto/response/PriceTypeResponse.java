package com.parsun.parsun_api.price_types.application.dto.response;

import com.parsun.parsun_api.price_types.domain.entities.PriceType;

import java.time.LocalDateTime;

public record PriceTypeResponse(
    String id,
    String name,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PriceTypeResponse from(PriceType priceType) {
        return new PriceTypeResponse(
            priceType.getId().toString(),
            priceType.getName(),
            priceType.getDescription(),
            priceType.isActive(),
            priceType.getCreatedAt(),
            priceType.getUpdatedAt()
        );
    }
}
