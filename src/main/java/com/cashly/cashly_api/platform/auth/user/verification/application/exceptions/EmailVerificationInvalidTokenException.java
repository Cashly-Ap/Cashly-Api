package com.cashly.cashly_api.platform.auth.user.verification.application.exceptions;

public class EmailVerificationInvalidTokenException extends RuntimeException {

    public EmailVerificationInvalidTokenException(String message) {
        super(message);
    }
}
