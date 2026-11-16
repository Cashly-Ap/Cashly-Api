package com.cashly.cashly_api.platform.notifications.domain.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record NotificationPreferences(boolean enabled,
        Map<String, Boolean> modules) {

    public NotificationPreferences {
        modules = modules == null
                ? Map.of()
                : Collections.unmodifiableMap(
                        new java.util.HashMap<>(modules));
    }

    public static NotificationPreferences defaults() {
        return new NotificationPreferences(true, Map.of());
    }

    public boolean moduleEnabled(String module) {
        return modules.getOrDefault(module, true);
    }

    public NotificationPreferences with(boolean masterEnabled,
            Map<String, Boolean> updatedModules) {
        var result = new java.util.HashMap<String, Boolean>();
        result.putAll(modules);
        result.putAll(updatedModules);
        return new NotificationPreferences(masterEnabled, result);
    }
}
