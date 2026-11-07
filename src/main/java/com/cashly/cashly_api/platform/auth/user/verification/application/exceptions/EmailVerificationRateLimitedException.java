package com.cashly.cashly_api.platform.auth.user.verification.application.exceptions;

public class EmailVerificationRateLimitedException extends RuntimeException {

    public EmailVerificationRateLimitedException(String message) {
        super(message);
    }
}
