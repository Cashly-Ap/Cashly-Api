package com.parsun.parsun_api.quotes.domain.valueobjects;

import java.util.UUID;

public record QuoteAllocationId(UUID value) {

    public static QuoteAllocationId generate() {
        return new QuoteAllocationId(UUID.randomUUID());
    }

    public static QuoteAllocationId of(UUID value) {
        return new QuoteAllocationId(value);
    }
}
