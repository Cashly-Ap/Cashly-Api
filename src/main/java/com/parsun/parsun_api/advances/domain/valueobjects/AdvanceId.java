package com.parsun.parsun_api.advances.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record AdvanceId(UUID value) {

    public AdvanceId {
        Objects.requireNonNull(value, "AdvanceId cannot be null");
    }

    public static AdvanceId of(UUID value) {
        return new AdvanceId(value);
    }

    public static AdvanceId generate() {
        return new AdvanceId(UUID.randomUUID());
    }
}
