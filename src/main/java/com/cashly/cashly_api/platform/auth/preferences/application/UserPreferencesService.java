package com.cashly.cashly_api.platform.auth.preferences.application;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferences;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;

public class UserPreferencesService {
    private final UserPreferencesRepository repository;
    private final CurrentActor actor;
    public UserPreferencesService(UserPreferencesRepository repository,
            CurrentActor actor) {
        this.repository = repository;
        this.actor = actor;
    }
    public UserPreferences current() {
        return repository.findByUserId(actor.requireId())
                .orElseGet(UserPreferences::defaults);
    }
    public UserPreferences update(UserPreferences preferences,
            String language) {
        String effectiveLanguage = language == null
                ? repository.findByUserId(actor.requireId())
                        .orElseGet(UserPreferences::defaults).language()
                : language;
        return update(preferences.withLanguage(effectiveLanguage));
    }
    public UserPreferences update(UserPreferences preferences) {
        repository.save(actor.requireId(), preferences);
        return preferences;
    }
}
