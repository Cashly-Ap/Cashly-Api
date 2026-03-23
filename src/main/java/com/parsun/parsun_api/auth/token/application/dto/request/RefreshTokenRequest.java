package com.parsun.parsun_api.auth.token.application.dto.request;

public record RefreshTokenRequest(
    String refreshToken
) {
    public RefreshTokenRequest {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
    }
}
