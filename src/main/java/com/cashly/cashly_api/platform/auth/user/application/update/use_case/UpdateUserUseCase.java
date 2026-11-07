package com.cashly.cashly_api.platform.auth.user.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserUseCase {
    private final UserPreferencesRepository preferences;

    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateUserUseCase(UserRepository userRepository,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access,
            UserPreferencesRepository preferences,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.preferences = preferences;
        this.userRepository = userRepository;
        this.access = access;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse execute(String id, UpdateUserRequest request) {
        access.lockAdministration();
        access.require(
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule.USERS,
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.EDIT);
        access.validateAssignment(request.roleId(), request.departmentId());
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        user.updateUserName(new UserName(request.userName()));
        access.protectAdministrator(userId.getValue(), request.roleId(),
                request.active() == null ? user.isActive() : request.active());
        user.updateProfile(request.profile(), request.roleId(),
                request.departmentId());

        if (request.active() != null) {
            if (request.active()) {
                user.activate();
            } else {
                user.deactivate();
            }
        }

        if (request.emailVerified() != null) {
            user.setEmailVerified(request.emailVerified());
        }

        User savedUser = userRepository.save(user);
        if (request.language() != null)
            preferences.updateLanguage(savedUser.getId().getValue(),
                    request.language());
        activityNotifications.record(ActivityEvent.of("USERS",
                "EDIT", "USER", savedUser.getId().getValue(),
                savedUser.getEmail().getValue(), null));
        return UserResponse.from(savedUser).withLanguage(
                preferences.languageFor(savedUser.getId().getValue()));
    }
}
