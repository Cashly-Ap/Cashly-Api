package com.cashly.cashly_api.invoices.domain.valueobjects;

import java.util.Objects;

public record InvoiceNumber(String value) {

    private static final String PREFIX = "INV";
    private static final String FORMAT = "%s-%d-%05d";

    public InvoiceNumber {
        Objects.requireNonNull(value, "InvoiceNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("InvoiceNumber cannot be blank");
        }
    }

    public static InvoiceNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new InvoiceNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
