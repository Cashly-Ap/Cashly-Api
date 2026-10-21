package com.cashly.cashly_api.platform.auth.authentication.application.dto.response;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    AuthenticatedEntityResponse entity,
    boolean mfaRequired,
    String challengeToken
) {
    public AuthenticationResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, AuthenticatedEntityResponse entity) {
        this(accessToken, refreshToken, tokenType, expiresIn, entity, false, null);
    }

    public static AuthenticationResponse mfaChallenge(String challengeToken) {
        return new AuthenticationResponse(null, null, null, 300, null, true, challengeToken);
    }

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
            entity,
            false,
            null
        );
    }
}
