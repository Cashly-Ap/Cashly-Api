package com.parsun.parsun_api.purchases.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record PurchaseLineId(UUID value) {

    public PurchaseLineId {
        Objects.requireNonNull(value, "PurchaseLineId cannot be null");
    }

    public static PurchaseLineId generate() {
        return new PurchaseLineId(UUID.randomUUID());
    }

    public static PurchaseLineId of(UUID value) {
        return new PurchaseLineId(value);
    }
}
