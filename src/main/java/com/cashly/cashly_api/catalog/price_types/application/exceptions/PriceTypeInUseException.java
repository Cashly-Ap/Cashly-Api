package com.cashly.cashly_api.catalog.price_types.application.exceptions;

import java.util.UUID;

public class PriceTypeInUseException extends RuntimeException {
    public PriceTypeInUseException(UUID id) {
        super("Price type is in use by products and cannot be deleted: " + id);
    }
}
