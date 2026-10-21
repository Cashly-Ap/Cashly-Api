package com.cashly.cashly_api.platform.auth.preferences.infrastructure.config;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.auth.preferences.application.UserPreferencesService;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserPreferencesConfig {
    @Bean
    UserPreferencesService userPreferencesService(UserPreferencesRepository repository, CurrentActor actor) {
        return new UserPreferencesService(repository, actor);
    }
}
