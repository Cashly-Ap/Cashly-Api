package com.cashly.cashly_api.platform.auth.role.domain.entity;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionRegistry;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Role(UUID id, String name, String description, boolean active, long version, String systemKey,
                   Map<BusinessModule, Set<PermissionAction>> permissions) {
    public Role {
        Objects.requireNonNull(id);
        name = UserProfile.text(name, 100, true);
        description = UserProfile.text(description, 500, false);
        permissions = PermissionRegistry.validate(permissions);
    }

    public boolean systemAdministrator() {
        return "SYSTEM_ADMIN".equals(systemKey);
    }

    public Map<BusinessModule, Set<PermissionAction>> permissions() {
        return systemAdministrator() ? PermissionRegistry.modules() : permissions;
    }

    public Map<BusinessModule, Set<PermissionAction>> effectivePermissions() {
        return permissions();
    }
}
