package com.cashly.cashly_api.products.domain.exceptions;

import java.util.UUID;

public class ProductReferencedException extends RuntimeException {
    public ProductReferencedException(UUID id, Throwable cause) {
        super("Product with id " + id + " has associated records", cause);
    }
}
