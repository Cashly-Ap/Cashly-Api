package com.cashly.cashly_api.auth.user.application.register;

import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;

public record RegisterUserRequest(
    String email,
    String userName,
    String password,
    UserRole role
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
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        validateRole(role);
    }

    private static void validateRole(UserRole role) {
        for (UserRole validRole : UserRole.values()) {
            if (validRole == role) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}
