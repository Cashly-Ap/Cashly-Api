package com.cashly.cashly_api.platform.auth.authentication.application.exceptions;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
