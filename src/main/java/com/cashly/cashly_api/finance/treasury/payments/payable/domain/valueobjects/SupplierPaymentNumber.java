package com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects;

import java.util.Objects;

public record SupplierPaymentNumber(String value) {

    private static final String PREFIX = "SPAY";
    private static final String FORMAT = "%s-%d-%04d";

    public SupplierPaymentNumber {
        Objects.requireNonNull(value, "SupplierPaymentNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "SupplierPaymentNumber cannot be blank");
        }
    }

    public static SupplierPaymentNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException(
                    "Sequence must be greater than zero");
        }
        return new SupplierPaymentNumber(
                String.format(FORMAT, PREFIX, year, sequence));
    }
}
