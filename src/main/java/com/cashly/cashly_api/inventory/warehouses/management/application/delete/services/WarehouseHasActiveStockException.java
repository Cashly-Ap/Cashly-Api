package com.cashly.cashly_api.inventory.warehouses.management.application.delete.services;

import java.util.UUID;

public class WarehouseHasActiveStockException extends RuntimeException {
    public WarehouseHasActiveStockException(UUID id) {
        super("Warehouse " + id + " has active stock and cannot be deleted");
    }
}
