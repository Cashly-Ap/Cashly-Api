package com.cashly.cashly_api.platform.auth.user.application.queries.use_case;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetUserByIdUseCase {
    private final UserPreferencesRepository preferences;


    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository, UserPreferencesRepository preferences) {
        this.preferences = preferences;
        this.userRepository = userRepository;
    }

    public UserResponse execute(String userId) {
        UserId id = UserId.from(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return UserResponse.from(user).withLanguage(preferences.languageFor(user.getId().getValue()));
    }
}
