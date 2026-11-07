package com.cashly.cashly_api.platform.auth.user.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserHasAssociatedRecordsException;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteUserUseCase(UserRepository userRepository,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.userRepository = userRepository;
        this.access = access;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void execute(String id) {
        access.lockAdministration();
        access.require(
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule.USERS,
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.DELETE);
        access.protectAdministrator(UUID.fromString(id), null, false);
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        if (userRepository.hasAssociatedRecords(userId)) {
            throw new UserHasAssociatedRecordsException(id);
        }

        activityNotifications.record(ActivityEvent.of("USERS",
                "DELETE", "USER", user.getId().getValue(),
                user.getEmail().getValue(), null));
        userRepository.delete(user);
    }
}
