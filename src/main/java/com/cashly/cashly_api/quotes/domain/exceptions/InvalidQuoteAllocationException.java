package com.cashly.cashly_api.quotes.domain.exceptions;

public class InvalidQuoteAllocationException extends RuntimeException {

    public InvalidQuoteAllocationException(String message) {
        super(message);
    }
}
