package com.parsun.parsun_api.orders.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record OrderLineId(UUID value) {

    public OrderLineId {
        Objects.requireNonNull(value, "OrderLineId cannot be null");
    }

    public static OrderLineId generate() {
        return new OrderLineId(UUID.randomUUID());
    }

    public static OrderLineId of(UUID value) {
        return new OrderLineId(value);
    }
}
