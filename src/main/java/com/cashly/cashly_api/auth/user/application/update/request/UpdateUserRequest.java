package com.cashly.cashly_api.auth.user.application.update.request;

import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "User name is required")
    @Size(min = 2, max = 100, message = "User name must be between 2 and 100 characters")
    String userName,

    @NotNull(message = "Role is required")
    UserRole role,

    Boolean active,
    Boolean emailVerified
) {}