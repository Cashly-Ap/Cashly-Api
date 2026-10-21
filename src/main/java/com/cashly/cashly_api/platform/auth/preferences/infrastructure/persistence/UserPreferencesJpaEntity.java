package com.cashly.cashly_api.platform.auth.preferences.infrastructure.persistence;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferences;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "auth_user_preferences")
public class UserPreferencesJpaEntity {
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;
    @Column(name = "theme_mode", nullable = false, length = 10)
    private String themeMode;
    @Column(name = "color_mode", nullable = false, length = 15)
    private String colorMode;
    @Column(name = "shortcuts_enabled", nullable = false)
    private boolean shortcutsEnabled;
    @Column(name = "show_shortcut_hints", nullable = false)
    private boolean showShortcutHints;
    protected UserPreferencesJpaEntity() {}
    public UserPreferences toDomain() {
        return new UserPreferences(themeMode, colorMode, shortcutsEnabled, showShortcutHints);
    }
}
