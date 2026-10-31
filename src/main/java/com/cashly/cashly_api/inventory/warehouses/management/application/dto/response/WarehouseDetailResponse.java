package com.cashly.cashly_api.inventory.warehouses.management.application.dto.response;

import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;

import java.time.LocalDateTime;

public record WarehouseDetailResponse(String id, String code, String name,
        String address, String state, String municipality, String postalCode,
        boolean active, boolean deletable, LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static WarehouseDetailResponse from(Warehouse w) {
        return from(w, true);
    }

    public static WarehouseDetailResponse from(Warehouse w, boolean deletable) {
        return new WarehouseDetailResponse(w.getId().toString(), w.getCode(),
                w.getName(), w.getAddress(), w.getState(), w.getMunicipality(),
                w.getPostalCode(), w.isActive(), deletable, w.getCreatedAt(),
                w.getUpdatedAt());
    }
}
