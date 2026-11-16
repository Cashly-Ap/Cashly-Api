package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.notifications.domain.entity.NotificationPreferences;
import com.cashly.cashly_api.platform.notifications.application.ports.out.PermissionAccessGateway;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationPreferencesService {

    private final PermissionAccessGateway access;
    private final NotificationPreferencesRepository repository;

    public NotificationPreferencesService(PermissionAccessGateway access,
            NotificationPreferencesRepository repository) {
        this.access = access;
        this.repository = repository;
    }

    public record ModulePreference(String module, String labelKey,
            boolean enabled) {
    }

    public record PreferencesView(boolean enabled,
            List<ModulePreference> modules) {
    }

    @Transactional(readOnly = true)
    public PreferencesView get(UUID userId) {
        var current = access.resolve(userId);
        var preferences = repository.find(userId)
                .orElseGet(NotificationPreferences::defaults);
        var eligible = eligibleModules(current);
        var modules = eligible.stream()
                .map(module -> new ModulePreference(module,
                        access.moduleLabelKey(module),
                        preferences.moduleEnabled(module)))
                .toList();
        return new PreferencesView(preferences.enabled(), modules);
    }

    @Transactional
    public PreferencesView update(UUID userId, boolean enabled,
            Map<String, Boolean> requestedModules) {
        var current = access.resolve(userId);
        var eligible = eligibleModules(current);
        if (requestedModules != null) {
            for (var module : requestedModules.keySet()) {
                if (!eligible.contains(module)) {
                    throw new IllegalArgumentException(
                            "Module is not eligible for notifications: "
                                    + module);
                }
            }
        }
        var existing = repository.find(userId)
                .orElseGet(NotificationPreferences::defaults);
        var modules = new java.util.HashMap<String, Boolean>();
        if (requestedModules != null) {
            modules.putAll(requestedModules);
        }
        repository.save(userId, existing.with(enabled, modules));
        return get(userId);
    }

    private List<String> eligibleModules(
            PermissionAccessGateway.Access current) {
        if (current.systemAdministrator()) {
            return access.notificationEligibleModules();
        }
        return current.permissions().stream()
                .map(code -> code.substring(0, code.lastIndexOf('_')))
                .distinct().filter(module -> {
                    var prefix = module + "_";
                    return current.permissions().stream()
                            .filter(code -> code.startsWith(prefix))
                            .map(code -> code.substring(prefix.length()))
                            .anyMatch(com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityActionPolicy::isMutating);
                }).sorted().toList();
    }
}
