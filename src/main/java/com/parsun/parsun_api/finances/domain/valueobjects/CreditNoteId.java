package com.parsun.parsun_api.finances.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record CreditNoteId(UUID value) {

    public CreditNoteId {
        Objects.requireNonNull(value, "CreditNoteId cannot be null");
    }

    public static CreditNoteId of(UUID value) {
        return new CreditNoteId(value);
    }

    public static CreditNoteId generate() {
        return new CreditNoteId(UUID.randomUUID());
    }
}
