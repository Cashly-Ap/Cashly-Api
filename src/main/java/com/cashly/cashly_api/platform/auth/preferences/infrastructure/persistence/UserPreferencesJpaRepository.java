package com.cashly.cashly_api.platform.auth.preferences.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface UserPreferencesJpaRepository
        extends
            JpaRepository<UserPreferencesJpaEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into auth_user_preferences (user_id, theme_mode, color_mode, shortcuts_enabled, show_shortcut_hints, language)
            values (:userId, :themeMode, :colorMode, :shortcutsEnabled, :showShortcutHints, :language)
            on duplicate key update theme_mode = :themeMode, color_mode = :colorMode,
                shortcuts_enabled = :shortcutsEnabled, show_shortcut_hints = :showShortcutHints, language = :language
            """, nativeQuery = true)
    void upsert(@Param("userId") byte[] userId,
            @Param("themeMode") String themeMode,
            @Param("colorMode") String colorMode,
            @Param("shortcutsEnabled") boolean shortcutsEnabled,
            @Param("showShortcutHints") boolean showShortcutHints,
            @Param("language") String language);
}
