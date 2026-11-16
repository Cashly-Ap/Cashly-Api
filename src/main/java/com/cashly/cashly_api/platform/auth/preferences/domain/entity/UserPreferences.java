package com.cashly.cashly_api.platform.auth.preferences.domain.entity;

import java.util.Set;

import com.cashly.cashly_api.platform.auth.preferences.domain.valueobjects.SupportedLanguage;
public record UserPreferences(String themeMode, String colorMode,
        boolean shortcutsEnabled, boolean showShortcutHints, String language) {
    public UserPreferences(String themeMode, String colorMode,
            boolean shortcutsEnabled, boolean showShortcutHints) {
        this(themeMode, colorMode, shortcutsEnabled, showShortcutHints, "es");
    }
    public UserPreferences {
        language = SupportedLanguage.require(language);
        if (themeMode == null
                || !Set.of("system", "light", "dark").contains(themeMode)) {
            throw new IllegalArgumentException("Unsupported theme mode");
        }
        if (colorMode == null
                || !Set.of("minimal", "informative").contains(colorMode)) {
            throw new IllegalArgumentException("Unsupported color mode");
        }
    }
    public UserPreferences withLanguage(String language) {
        return new UserPreferences(themeMode, colorMode, shortcutsEnabled,
                showShortcutHints, language);
    }
    public static UserPreferences defaults() {
        return new UserPreferences("system", "minimal", false, true);
    }
}
