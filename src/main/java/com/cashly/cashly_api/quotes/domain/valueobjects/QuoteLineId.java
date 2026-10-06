package com.cashly.cashly_api.quotes.domain.valueobjects;

import java.util.UUID;

public record QuoteLineId(UUID value) {

    public static QuoteLineId generate() {
        return new QuoteLineId(UUID.randomUUID());
    }

    public static QuoteLineId of(UUID value) {
        return new QuoteLineId(value);
    }
}
