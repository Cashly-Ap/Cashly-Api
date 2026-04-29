package com.parsun.parsun_api.price_types.application.exceptions;

public class DuplicatePriceTypeNameException extends RuntimeException {
    public DuplicatePriceTypeNameException(String name) {
        super("Price type with name already exists: " + name);
    }
}
