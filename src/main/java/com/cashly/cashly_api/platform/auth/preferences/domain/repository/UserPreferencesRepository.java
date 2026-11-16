package com.cashly.cashly_api.platform.auth.preferences.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.cashly.cashly_api.platform.auth.preferences.domain.entity.UserPreferences;
public interface UserPreferencesRepository {
    Optional<UserPreferences> findByUserId(UUID userId);
    void save(UUID userId, UserPreferences preferences);
    default UserPreferences preferencesFor(UUID userId) {
        return findByUserId(userId).orElseGet(UserPreferences::defaults);
    }
    default String languageFor(UUID userId) {
        return preferencesFor(userId).language();
    }
    default void updateLanguage(UUID userId, String language) {
        save(userId, preferencesFor(userId).withLanguage(language));
    }
    java.util.Map<UUID, String> languagesFor(java.util.Set<UUID> userIds);

}
