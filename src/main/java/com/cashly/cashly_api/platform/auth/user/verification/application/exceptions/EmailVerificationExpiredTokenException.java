package com.cashly.cashly_api.platform.auth.user.verification.application.exceptions;

public class EmailVerificationExpiredTokenException extends RuntimeException {

    public EmailVerificationExpiredTokenException(String message) {
        super(message);
    }
}
