package com.parsun.parsun_api.auth.portal.application.dto.request;

public record PortalLoginRequest(
    String rif,
    String password
) {
    public PortalLoginRequest {
        if (rif == null || rif.trim().isEmpty()) {
            throw new IllegalArgumentException("RIF is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
