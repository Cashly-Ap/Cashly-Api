package com.parsun.parsun_api.auth.portal.application.dto.response;

public record PortalAuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    PortalCustomerResponse customer
) {
    public static PortalAuthResponse of(
        String accessToken,
        String refreshToken,
        long expiresIn,
        PortalCustomerResponse customer
    ) {
        return new PortalAuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn,
            customer
        );
    }
}
