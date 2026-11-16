package com.cashly.cashly_api.sales.quotes.domain.exceptions;

public class InvalidQuoteLineException extends RuntimeException {

    public InvalidQuoteLineException(String message) {
        super(message);
    }
}
