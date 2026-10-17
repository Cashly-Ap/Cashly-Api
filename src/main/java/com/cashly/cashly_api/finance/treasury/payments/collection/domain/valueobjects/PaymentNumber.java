package com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects;

import java.util.Objects;

public record PaymentNumber(String value) {

    private static final String PREFIX = "PAY";
    private static final String FORMAT = "%s-%d-%04d";

    public PaymentNumber {
        Objects.requireNonNull(value, "PaymentNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PaymentNumber cannot be blank");
        }
    }

    public static PaymentNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new PaymentNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
