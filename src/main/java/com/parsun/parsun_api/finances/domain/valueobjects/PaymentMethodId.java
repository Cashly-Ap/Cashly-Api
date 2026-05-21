package com.parsun.parsun_api.finances.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record PaymentMethodId(UUID value) {

    public PaymentMethodId {
        Objects.requireNonNull(value, "PaymentMethodId cannot be null");
    }

    public static PaymentMethodId of(UUID value) {
        return new PaymentMethodId(value);
    }

    public static PaymentMethodId generate() {
        return new PaymentMethodId(UUID.randomUUID());
    }
}
