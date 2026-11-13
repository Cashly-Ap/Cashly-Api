package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.ADMIN_USER;
import static com.cashly.cashly_api.infrastructure.seed.SeedIds.OPERATOR_USER;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.preferences.domain.entity.UserPreferences;
import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import org.springframework.stereotype.Component;

@Component
public class V29UserPreferencesSeed implements SeedStep {
    private final UserPreferencesRepository repository;
    public V29UserPreferencesSeed(UserPreferencesRepository repository) {
        this.repository = repository;
    }
    @Override
    public int version() {
        return 29;
    }
    @Override
    public void seed() {
        repository.save(ADMIN_USER, new UserPreferences("dark", "minimal",
                false, false, "es"));
        repository.save(OPERATOR_USER, new UserPreferences("system", "minimal",
                true, false, "es"));
    }
}
