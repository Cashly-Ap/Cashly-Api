package com.cashly.cashly_api.quotes.domain.exceptions;

import java.util.UUID;

public class QuoteNotFoundException extends RuntimeException {

    public QuoteNotFoundException(UUID id) {
        super("Quote not found with id: " + id);
    }
}
