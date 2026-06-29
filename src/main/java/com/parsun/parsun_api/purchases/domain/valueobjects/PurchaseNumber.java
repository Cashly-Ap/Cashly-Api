package com.parsun.parsun_api.purchases.domain.valueobjects;

import java.util.Objects;

public record PurchaseNumber(String value) {

    private static final String PREFIX = "PUR";
    private static final String FORMAT = "%s-%d-%05d";

    public PurchaseNumber {
        Objects.requireNonNull(value, "PurchaseNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PurchaseNumber cannot be blank");
        }
    }

    public static PurchaseNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new PurchaseNumber(String.format(FORMAT, PREFIX, year, sequence));
    }

    public String getValue() {
        return value;
    }
}
