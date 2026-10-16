package com.cashly.cashly_api.warehouses.management.application.exceptions;

import java.util.UUID;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(UUID id) {
        super("Warehouse not found with id: " + id);
    }
}
