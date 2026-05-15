package com.parsun.parsun_api.quotes.domain.exceptions;

public class InvalidQuoteLineException extends RuntimeException {

    public InvalidQuoteLineException(String message) {
        super(message);
    }
}
