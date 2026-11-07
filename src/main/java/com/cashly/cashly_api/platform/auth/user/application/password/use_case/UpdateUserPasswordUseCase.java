package com.cashly.cashly_api.platform.auth.user.application.password.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.platform.auth.user.application.password.request.UpdateUserPasswordRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserPasswordUseCase {
    private final UserPreferencesRepository preferences;

    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;
    private final PasswordEncoder passwordEncoder;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateUserPasswordUseCase(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access,
            UserPreferencesRepository preferences,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.preferences = preferences;
        this.userRepository = userRepository;
        this.access = access;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse execute(String id, UpdateUserPasswordRequest request) {
        access.authorizeAccountMutation(UUID.fromString(id), false);
        var response = change(id, request);
        activityNotifications.record(ActivityEvent.of("USERS",
                "EDIT", "USER", UUID.fromString(id), id, null));
        return response;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse executeSelf(String id,
            UpdateUserPasswordRequest request) {
        access.authorizeAccountMutation(UUID.fromString(id), true);
        if (request.currentPassword() == null
                || request.currentPassword().isBlank()) {
            throw new InvalidCredentialsException(
                    "Current password is required");
        }
        return change(id, request);
    }

    private UserResponse change(String id, UpdateUserPasswordRequest request) {
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));
        if (request.currentPassword() != null
                && !request.currentPassword().isBlank()
                && !passwordEncoder.matches(request.currentPassword(),
                        user.getPassword().getHashedValue())) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect");
        }
        Password.validateRawPassword(request.password());
        user.changePassword(
                Password.fromHash(passwordEncoder.encode(request.password())));
        return UserResponse.from(userRepository.save(user))
                .withLanguage(preferences.languageFor(user.getId().getValue()));
    }
}
