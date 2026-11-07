package com.cashly.cashly_api.platform.auth.user.application.email.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.email.request.UpdateUserEmailRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.platform.auth.user.verification.application.services.EmailVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserEmailUseCase {
    private final UserPreferencesRepository preferences;

    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    private final ActivityNotificationRecorder activityNotifications;
    private final EmailVerificationService emailVerification;

    public UpdateUserEmailUseCase(UserRepository userRepository,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access,
            UserPreferencesRepository preferences,
            ActivityNotificationRecorder activityNotifications,
            EmailVerificationService emailVerification) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.preferences = preferences;
        this.userRepository = userRepository;
        this.access = access;
        this.emailVerification = emailVerification;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse execute(String id, UpdateUserEmailRequest request) {
        access.authorizeAccountMutation(UUID.fromString(id), false);
        return change(id, request);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse executeSelf(String id, UpdateUserEmailRequest request) {
        UUID userId = UUID.fromString(id);
        access.authorizeAccountMutation(userId, true);
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));
        String currentEmail = request.currentEmail();
        if (currentEmail == null || currentEmail.isBlank()
                || !user.getEmail().getValue()
                        .equalsIgnoreCase(currentEmail.trim())) {
            throw new InvalidCredentialsException(
                    "Current email is incorrect");
        }
        return change(id, request);
    }

    private UserResponse change(String id, UpdateUserEmailRequest request) {
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        Email newEmail = new Email(request.email());

        if (userRepository.existsByEmail(newEmail)
                && !user.getEmail().equals(newEmail)) {
            throw new DuplicateEmailException(
                    "Email already in use: " + request.email());
        }

        user.updateEmail(newEmail);
        User saved = userRepository.save(user);
        activityNotifications.record(ActivityEvent.of("USERS",
                "EDIT", "USER", saved.getId().getValue(),
                saved.getEmail().getValue(), null));
        emailVerification.requestVerification(saved, preferences
                .languageFor(saved.getId().getValue()));
        return UserResponse.from(saved).withLanguage(
                preferences.languageFor(saved.getId().getValue()));
    }
}
