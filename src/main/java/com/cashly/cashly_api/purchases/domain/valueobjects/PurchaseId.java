package com.cashly.cashly_api.purchases.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record PurchaseId(UUID value) {

    public PurchaseId {
        Objects.requireNonNull(value, "PurchaseId cannot be null");
    }

    public static PurchaseId generate() {
        return new PurchaseId(UUID.randomUUID());
    }

    public static PurchaseId of(UUID value) {
        return new PurchaseId(value);
    }

    public UUID getValue() {
        return value;
    }
}
