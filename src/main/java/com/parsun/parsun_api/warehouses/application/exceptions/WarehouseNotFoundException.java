package com.parsun.parsun_api.warehouses.application.exceptions;

import java.util.UUID;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(UUID id) {
        super("Warehouse not found with id: " + id);
    }
}
