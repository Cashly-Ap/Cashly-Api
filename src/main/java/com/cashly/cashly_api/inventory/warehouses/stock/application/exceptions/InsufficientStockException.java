package com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, UUID warehouseId, int available, int requested) {
        super("Insufficient stock for product " + productId + " in warehouse " + warehouseId
                + ": available=" + available + ", requested=" + requested);
    }
}
