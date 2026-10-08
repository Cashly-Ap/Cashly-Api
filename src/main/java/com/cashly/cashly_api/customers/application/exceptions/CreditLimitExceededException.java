package com.cashly.cashly_api.customers.application.exceptions;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}
