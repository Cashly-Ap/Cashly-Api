package com.cashly.cashly_api.inventory.warehouses.management.application.dto.response;

import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;

import java.time.LocalDateTime;

public record WarehouseListResponse(String id, String code, String name,
        String state, String municipality, boolean active, boolean deletable,
        LocalDateTime createdAt) {
    public static WarehouseListResponse from(Warehouse w) {
        return from(w, true);
    }

    public static WarehouseListResponse from(Warehouse w, boolean deletable) {
        return new WarehouseListResponse(w.getId().toString(), w.getCode(),
                w.getName(), w.getState(), w.getMunicipality(), w.isActive(),
                deletable, w.getCreatedAt());
    }
}
