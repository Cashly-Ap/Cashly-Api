package com.cashly.cashly_api.orders.domain.valueobjects;

import java.util.Objects;

public record OrderNumber(String value) {

    private static final String PREFIX = "ORD";
    private static final String FORMAT = "%s-%d-%05d";

    public OrderNumber {
        Objects.requireNonNull(value, "OrderNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrderNumber cannot be blank");
        }
    }

    public static OrderNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new OrderNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
