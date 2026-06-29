package com.parsun.parsun_api.purchases.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record PurchaseLineAllocationId(UUID value) {

    public PurchaseLineAllocationId {
        Objects.requireNonNull(value, "PurchaseLineAllocationId cannot be null");
    }

    public static PurchaseLineAllocationId generate() {
        return new PurchaseLineAllocationId(UUID.randomUUID());
    }

    public static PurchaseLineAllocationId of(UUID value) {
        return new PurchaseLineAllocationId(value);
    }
}
