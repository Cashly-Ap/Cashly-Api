package com.cashly.cashly_api.catalog.products.application.exceptions;

public class DuplicateProductCodeException extends RuntimeException {
    public DuplicateProductCodeException(String code) {
        super("Product with code already exists: " + code);
    }
}
