package com.cashly.cashly_api.auth.authentication.application.login.request;

public record LoginRequest(
    String email,
    String password
) {
    public LoginRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
