package com.cashly.cashly_api.platform.notifications.domain.repository;

import com.cashly.cashly_api.platform.notifications.domain.entity.NotificationPreferences;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface NotificationPreferencesRepository {
    Optional<NotificationPreferences> find(UUID userId);
    void save(UUID userId, NotificationPreferences preferences);
    Map<UUID, NotificationPreferences> findAll(Set<UUID> userIds);
}
