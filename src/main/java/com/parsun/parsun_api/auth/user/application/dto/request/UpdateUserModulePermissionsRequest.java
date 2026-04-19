package com.parsun.parsun_api.auth.user.application.dto.request;

import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserModulePermissionsRequest(
    @NotEmpty(message = "At least one module permission is required") Set<BusinessModule> modulePermissions
) {}
