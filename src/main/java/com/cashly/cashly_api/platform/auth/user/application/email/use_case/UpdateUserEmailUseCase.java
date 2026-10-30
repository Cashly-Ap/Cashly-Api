package com.cashly.cashly_api.platform.auth.user.application.email.use_case;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.email.request.UpdateUserEmailRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserEmailUseCase {
    private final UserPreferencesRepository preferences;


    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    public UpdateUserEmailUseCase(UserRepository userRepository,com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access, UserPreferencesRepository preferences) {
        this.preferences = preferences;
        this.userRepository = userRepository;
        this.access=access;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse execute(String id, UpdateUserEmailRequest request) {
        access.authorizeAccountMutation(UUID.fromString(id), false);
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        Email newEmail = new Email(request.email());

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new DuplicateEmailException("Email already in use: " + request.email());
        }

        user.updateEmail(newEmail);
        User saved = userRepository.save(user);
        return UserResponse.from(saved).withLanguage(preferences.languageFor(saved.getId().getValue()));
    }
}
