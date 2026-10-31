package com.cashly.cashly_api.platform.auth.user.application.mappers;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private final UserPreferencesRepository preferences;

    public UserMapper(UserPreferencesRepository preferences) {
        this.preferences = preferences;
    }

    public UserResponse toResponse(User user) {
        return UserResponse.from(user)
                .withLanguage(preferences.languageFor(user.getId().getValue()));
    }
}
