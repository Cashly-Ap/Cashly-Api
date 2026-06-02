package com.parsun.parsun_api.shared.services.exceptions;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}
