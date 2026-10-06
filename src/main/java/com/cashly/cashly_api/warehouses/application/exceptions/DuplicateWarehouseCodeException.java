package com.cashly.cashly_api.warehouses.application.exceptions;

public class DuplicateWarehouseCodeException extends RuntimeException {
    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse with code already exists: " + code);
    }
}
