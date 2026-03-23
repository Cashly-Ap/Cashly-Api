package com.parsun.parsun_api.auth.user.application.dto.request;

public record RegisterUserRequest(
    String email,
    String userName,
    String password
) {
    public RegisterUserRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
