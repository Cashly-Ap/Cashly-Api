package com.parsun.parsun_api.credit_notes.domain.valueobjects;

import java.util.Objects;

public record CreditNoteNumber(String value) {

    private static final String PREFIX = "CN";
    private static final String FORMAT = "%s-%d-%04d";

    public CreditNoteNumber {
        Objects.requireNonNull(value, "CreditNoteNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CreditNoteNumber cannot be blank");
        }
    }

    public static CreditNoteNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new CreditNoteNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
