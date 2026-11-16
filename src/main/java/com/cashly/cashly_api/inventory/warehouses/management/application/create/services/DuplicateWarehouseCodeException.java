package com.cashly.cashly_api.inventory.warehouses.management.application.create.services;

public class DuplicateWarehouseCodeException extends RuntimeException {
    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse with code already exists: " + code);
    }
}
