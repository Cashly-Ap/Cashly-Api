package com.cashly.cashly_api.warehouses.application.ports.in;

import java.util.UUID;

public record WarehouseDirectoryEntry(UUID id, String name, String address) {
    public WarehouseDirectoryEntry(UUID id, String name) {
        this(id, name, null);
    }
}
