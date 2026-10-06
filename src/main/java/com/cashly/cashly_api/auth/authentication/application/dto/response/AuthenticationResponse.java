package com.cashly.cashly_api.auth.authentication.application.dto.response;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    AuthenticatedEntityResponse entity
) {
    public static AuthenticationResponse of(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthenticatedEntityResponse entity
    ) {
        return new AuthenticationResponse(
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn,
            entity
        );
    }
}
