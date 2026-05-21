package com.parsun.parsun_api.finances.domain.valueobjects;

import java.util.Objects;

public record DebitNoteNumber(String value) {

    private static final String PREFIX = "DN";
    private static final String FORMAT = "%s-%d-%04d";

    public DebitNoteNumber {
        Objects.requireNonNull(value, "DebitNoteNumber cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("DebitNoteNumber cannot be blank");
        }
    }

    public static DebitNoteNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        return new DebitNoteNumber(String.format(FORMAT, PREFIX, year, sequence));
    }
}
