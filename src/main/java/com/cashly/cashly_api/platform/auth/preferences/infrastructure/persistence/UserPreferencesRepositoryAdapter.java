package com.cashly.cashly_api.platform.auth.preferences.infrastructure.persistence;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferences;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPreferencesRepositoryAdapter implements UserPreferencesRepository {
    private final UserPreferencesJpaRepository repository;
    public UserPreferencesRepositoryAdapter(UserPreferencesJpaRepository repository) {
        this.repository = repository;
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<UserPreferences> findByUserId(UUID userId) {
        return repository.findById(userId).map(UserPreferencesJpaEntity::toDomain);
    }
    @Override
    @Transactional
    public void save(UUID userId, UserPreferences preferences) {
        byte[] binaryId = ByteBuffer.allocate(16).putLong(userId.getMostSignificantBits())
                .putLong(userId.getLeastSignificantBits()).array();
        repository.upsert(binaryId, preferences.themeMode(), preferences.colorMode(), preferences.shortcutsEnabled(), preferences.showShortcutHints());
    }
}
