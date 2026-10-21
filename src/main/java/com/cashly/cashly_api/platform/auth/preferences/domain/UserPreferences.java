package com.cashly.cashly_api.platform.auth.preferences.domain;

import java.util.Set;

public record UserPreferences(String themeMode, String colorMode, boolean shortcutsEnabled, boolean showShortcutHints) {
    public UserPreferences {
        if (themeMode == null || !Set.of("system", "light", "dark").contains(themeMode)) {
            throw new IllegalArgumentException("Unsupported theme mode");
        }
        if (colorMode == null || !Set.of("minimal", "informative").contains(colorMode)) {
            throw new IllegalArgumentException("Unsupported color mode");
        }
    }
    public static UserPreferences defaults() {
        return new UserPreferences("system", "minimal", false, true);
    }
}
