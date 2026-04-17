package com.parsun.parsun_api.auth.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordRequest(
    @NotBlank(message = "Password is required")
    String password
) {}
