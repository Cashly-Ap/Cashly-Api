package com.cashly.cashly_api.platform.notifications.infrastructure.persistence.adapters;

import com.cashly.cashly_api.platform.notifications.domain.entity.NotificationPreferences;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationPreferencesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class JdbcNotificationPreferencesRepository
        implements
            NotificationPreferencesRepository {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(JdbcNotificationPreferencesRepository.class);

    private final JdbcTemplate jdbc;

    public JdbcNotificationPreferencesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<NotificationPreferences> find(UUID userId) {
        var enabled = jdbc.query(
                "select enabled from notification_preferences where user_id=?",
                (rs, row) -> rs.getBoolean(1), bytes(userId)).stream()
                .findFirst();
        if (enabled.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NotificationPreferences(enabled.get(),
                modules(userId)));
    }

    @Override
    public Map<UUID, NotificationPreferences> findAll(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, NotificationPreferences> result = new HashMap<>();
        for (UUID userId : userIds) {
            find(userId).ifPresent(preferences -> result.put(userId,
                    preferences));
        }
        return result;
    }

    @Override
    public void save(UUID userId, NotificationPreferences preferences) {
        jdbc.update("""
                insert into notification_preferences(user_id,enabled,updated_at)
                values(?,?,current_timestamp)
                on duplicate key update enabled=values(enabled),updated_at=current_timestamp
                """, bytes(userId), preferences.enabled());
        preferences.modules().forEach((module, enabled) -> jdbc.update("""
                insert into notification_module_preferences(user_id,module,enabled,updated_at)
                values(?,?,?,current_timestamp)
                on duplicate key update enabled=values(enabled),updated_at=current_timestamp
                """, bytes(userId), module, enabled));
    }

    private Map<String, Boolean> modules(UUID userId) {
        Map<String, Boolean> modules = new HashMap<>();
        jdbc.query(
                "select module,enabled from notification_module_preferences where user_id=?",
                (rs, row) -> {
                    try {
                        modules.put(rs.getString("module"),
                                rs.getBoolean("enabled"));
                    } catch (IllegalArgumentException unknown) {
                        LOGGER.warn("Ignoring unknown notification module {}",
                                rs.getString("module"));
                    }
                    return null;
                }, bytes(userId));
        return modules;
    }

    private static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }
}
