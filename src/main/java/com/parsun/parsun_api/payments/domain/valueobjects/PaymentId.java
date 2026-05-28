package com.parsun.parsun_api.payments.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record PaymentId(UUID value) {

    public PaymentId {
        Objects.requireNonNull(value, "PaymentId cannot be null");
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }
}
