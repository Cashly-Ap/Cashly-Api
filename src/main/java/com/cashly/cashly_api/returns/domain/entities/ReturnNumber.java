package com.cashly.cashly_api.returns.domain.entities;

import java.util.Objects;

public record ReturnNumber(String value) {

    private static final String PREFIX = "RET";
    private static final String FORMAT = "%s-%d-%04d";

    public ReturnNumber {
        Objects.requireNonNull(value, "ReturnNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ReturnNumber cannot be blank");
        }
    }

    public static ReturnNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new ReturnNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
