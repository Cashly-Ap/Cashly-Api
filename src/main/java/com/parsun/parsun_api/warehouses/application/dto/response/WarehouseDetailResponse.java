package com.parsun.parsun_api.warehouses.application.dto.response;

import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;

import java.time.LocalDateTime;

public record WarehouseDetailResponse(
        String id,
        String code,
        String name,
        String address,
        String state,
        String municipality,
        String postalCode,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WarehouseDetailResponse from(Warehouse w) {
        return new WarehouseDetailResponse(
                w.getId().toString(), w.getCode(), w.getName(), w.getAddress(),
                w.getState(), w.getMunicipality(), w.getPostalCode(),
                w.isActive(), w.getCreatedAt(), w.getUpdatedAt()
        );
    }
}
