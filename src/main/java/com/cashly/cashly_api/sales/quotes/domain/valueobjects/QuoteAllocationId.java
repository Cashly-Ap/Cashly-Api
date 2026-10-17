package com.cashly.cashly_api.sales.quotes.domain.valueobjects;

import java.util.UUID;

public record QuoteAllocationId(UUID value) {

    public static QuoteAllocationId generate() {
        return new QuoteAllocationId(UUID.randomUUID());
    }

    public static QuoteAllocationId of(UUID value) {
        return new QuoteAllocationId(value);
    }
}
