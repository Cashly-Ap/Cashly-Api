package com.cashly.cashly_api.customers.warehouse_access.application.exceptions;

public class CustomerWarehouseAccessAlreadyExistsException extends RuntimeException {
    public CustomerWarehouseAccessAlreadyExistsException(java.util.UUID customerId, java.util.UUID warehouseId) {
        super("Customer " + customerId + " already has access to warehouse " + warehouseId);
    }
}
