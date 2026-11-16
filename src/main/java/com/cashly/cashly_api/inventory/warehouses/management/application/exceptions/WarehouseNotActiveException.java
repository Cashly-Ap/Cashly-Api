package com.cashly.cashly_api.inventory.warehouses.management.application.exceptions;

import java.util.UUID;

public class WarehouseNotActiveException extends RuntimeException {
    public WarehouseNotActiveException(UUID id) {
        super("Warehouse is not active: " + id);
    }
}
