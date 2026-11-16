package com.cashly.cashly_api.platform.notifications.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionCatalog;
import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionRegistry;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityActionPolicy;
import com.cashly.cashly_api.platform.notifications.application.ports.out.PermissionAccessGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LocalPermissionAccessGateway implements PermissionAccessGateway {

    private final AccessService access;

    public LocalPermissionAccessGateway(AccessService access) {
        this.access = access;
    }

    @Override
    public Access resolve(UUID userId) {
        var current = access.resolve(userId);
        return new Access(current.systemAdministrator(),
                current.permissions());
    }

    @Override
    public String moduleLabelKey(String module) {
        return PermissionCatalog.domainLabelKey(
                PermissionModule.valueOf(module));
    }

    @Override
    public List<String> notificationEligibleModules() {
        return PermissionRegistry.modules().entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(action -> ActivityActionPolicy
                                .isMutating(action.name())))
                .map(entry -> entry.getKey().name()).sorted().toList();
    }
}
