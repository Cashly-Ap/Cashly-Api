package com.parsun.parsun_api.advances.domain.valueobjects;

import java.util.Objects;

public record AdvanceNumber(String value) {

    private static final String PREFIX = "ADV";
    private static final String FORMAT = "%s-%d-%04d";

    public AdvanceNumber {
        Objects.requireNonNull(value, "AdvanceNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AdvanceNumber cannot be blank");
        }
    }

    public static AdvanceNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new AdvanceNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
