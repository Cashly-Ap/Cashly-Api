package com.cashly.cashly_api.cashregister.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record RegisterId(UUID value) {

    public RegisterId {
        Objects.requireNonNull(value, "RegisterId must not be null");
    }

    public static RegisterId generate() {
        return new RegisterId(UUID.randomUUID());
    }

    public static RegisterId of(UUID uuid) {
        return new RegisterId(uuid);
    }
}
