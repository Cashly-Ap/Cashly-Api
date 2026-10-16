package com.cashly.cashly_api.returns.domain;

import java.util.Objects;
import java.util.UUID;

public record ReturnId(UUID value) {

    public ReturnId {
        Objects.requireNonNull(value, "ReturnId cannot be null");
    }

    public static ReturnId of(UUID value) {
        return new ReturnId(value);
    }

    public static ReturnId generate() {
        return new ReturnId(UUID.randomUUID());
    }
}
