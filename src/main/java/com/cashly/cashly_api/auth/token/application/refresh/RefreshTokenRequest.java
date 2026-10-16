package com.cashly.cashly_api.auth.token.application.refresh;

public record RefreshTokenRequest(
    String refreshToken
) {
    public RefreshTokenRequest {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
    }
}
