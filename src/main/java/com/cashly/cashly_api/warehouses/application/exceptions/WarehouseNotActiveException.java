package com.cashly.cashly_api.warehouses.application.exceptions;

import java.util.UUID;

public class WarehouseNotActiveException extends RuntimeException {
    public WarehouseNotActiveException(UUID id) {
        super("Warehouse is not active: " + id);
    }
}
