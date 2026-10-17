package com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record DebitNoteId(UUID value) {

    public DebitNoteId {
        Objects.requireNonNull(value, "DebitNoteId cannot be null");
    }

    public static DebitNoteId of(UUID value) {
        return new DebitNoteId(value);
    }

    public static DebitNoteId generate() {
        return new DebitNoteId(UUID.randomUUID());
    }
}
