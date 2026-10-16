package com.cashly.cashly_api.customers.warehouse_access.application.exceptions;

public class CustomerWarehouseAccessNotFoundException extends RuntimeException {
    public CustomerWarehouseAccessNotFoundException(java.util.UUID customerId, java.util.UUID warehouseId) {
        super("Customer " + customerId + " does not have access to warehouse " + warehouseId);
    }
}
