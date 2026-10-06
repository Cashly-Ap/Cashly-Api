package com.cashly.cashly_api.warehouses.application.exceptions;

import java.util.UUID;

public class WarehouseHasActiveStockException extends RuntimeException {
    public WarehouseHasActiveStockException(UUID id) {
        super("Warehouse " + id + " has active stock and cannot be deleted");
    }
}
