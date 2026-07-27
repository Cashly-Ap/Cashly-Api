package com.parsun.parsun_api.portal.application.dto.response;

public record PortalAuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    CustomerSummaryResponse customer
) {}
