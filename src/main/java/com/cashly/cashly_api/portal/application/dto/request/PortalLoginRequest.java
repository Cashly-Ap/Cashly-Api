package com.cashly.cashly_api.portal.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PortalLoginRequest(
    @NotBlank String rif,
    @NotBlank String password
) {}
