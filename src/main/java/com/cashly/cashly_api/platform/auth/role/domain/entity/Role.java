package com.cashly.cashly_api.platform.auth.role.domain.entity;

import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionRegistry;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record Role(UUID id, String name, String description, boolean active,
        long version, String systemKey,
        Map<PermissionModule, Set<PermissionAction>> permissions) {
    public Role {
        Objects.requireNonNull(id);
        name = UserProfile.text(name, 100, true);
        description = UserProfile.text(description, 500, false);
        permissions = PermissionRegistry.validate(permissions);
    }

    public boolean systemAdministrator() {
        return "SYSTEM_ADMIN".equals(systemKey);
    }

    @JsonIgnore
    public Map<PermissionModule, Set<PermissionAction>> permissions() {
        return systemAdministrator()
                ? PermissionRegistry.modules()
                : permissions;
    }

    @JsonIgnore
    public Map<PermissionModule, Set<PermissionAction>> effectivePermissions() {
        return permissions();
    }

    @JsonProperty("permissions")
    public Set<String> codes() {
        return permissions().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(
                        action -> entry.getKey().name() + "_" + action.name()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
