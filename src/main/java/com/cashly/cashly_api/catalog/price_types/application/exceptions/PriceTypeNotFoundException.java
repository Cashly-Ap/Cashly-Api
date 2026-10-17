package com.cashly.cashly_api.catalog.price_types.application.exceptions;

import java.util.UUID;

public class PriceTypeNotFoundException extends RuntimeException {
    public PriceTypeNotFoundException(UUID id) {
        super("Price type not found with id: " + id);
    }
}
