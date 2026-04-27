package com.parsun.parsun_api.customers.application.exceptions;

public class CustomerWarehouseAccessAlreadyExistsException extends RuntimeException {
    public CustomerWarehouseAccessAlreadyExistsException(java.util.UUID customerId, java.util.UUID warehouseId) {
        super("Customer " + customerId + " already has access to warehouse " + warehouseId);
    }
}
