package com.cashly.cashly_api.purchases.domain.exceptions;

import java.util.UUID;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(UUID id) {
        super("Purchase not found with id: " + id);
    }
}
