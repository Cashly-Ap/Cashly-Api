package com.cashly.cashly_api.warehouses.stock.application.exceptions;

import java.util.UUID;

public class ProductNotInWarehouseException extends RuntimeException {
    public ProductNotInWarehouseException(UUID productId, UUID warehouseId) {
        super("Product " + productId + " has no stock record in warehouse " + warehouseId);
    }
}
