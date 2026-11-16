package com.cashly.cashly_api.platform.auth.token.application.logout.request;

public record LogoutRequest(String refreshToken) {
    public LogoutRequest {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
    }
}
