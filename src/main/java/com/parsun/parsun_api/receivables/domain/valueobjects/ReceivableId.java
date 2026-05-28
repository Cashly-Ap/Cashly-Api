package com.parsun.parsun_api.receivables.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record ReceivableId(UUID value) {

    public ReceivableId {
        Objects.requireNonNull(value, "ReceivableId cannot be null");
    }

    public static ReceivableId of(UUID value) {
        return new ReceivableId(value);
    }

    public static ReceivableId generate() {
        return new ReceivableId(UUID.randomUUID());
    }
}
