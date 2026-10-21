package com.cashly.cashly_api.platform.auth.preferences.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository {
    Optional<UserPreferences> findByUserId(UUID userId);
    void save(UUID userId, UserPreferences preferences);
}
