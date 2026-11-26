package com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, UUID warehouseId,
            BigDecimal available, BigDecimal requested) {
        super("Insufficient stock for product " + productId + " in warehouse "
                + warehouseId + ": available=" + available.toPlainString()
                + ", requested=" + requested.toPlainString());
    }
}
