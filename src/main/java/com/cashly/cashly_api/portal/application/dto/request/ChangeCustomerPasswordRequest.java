package com.cashly.cashly_api.portal.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeCustomerPasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    String newPassword
) {}
