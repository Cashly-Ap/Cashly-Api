package com.cashly.cashly_api.catalog.products.application.exceptions;

import java.util.UUID;

public class ProductHasStockException extends RuntimeException {
    public ProductHasStockException(UUID id) {
        super("Product with id " + id
                + " cannot be deleted because it has active stock in warehouses");
    }
}
