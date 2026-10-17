package com.cashly.cashly_api.platform.auth.user.application.permissions.request;

import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserModulePermissionsRequest(
    @NotEmpty(message = "At least one module permission is required") Set<BusinessModule> modulePermissions
) {}
