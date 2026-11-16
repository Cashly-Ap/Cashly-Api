package com.cashly.cashly_api.platform.auth.user.application.register.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.platform.auth.user.verification.application.services.EmailVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class RegisterUserUseCase {
    private final UserPreferencesRepository preferences;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    private final ActivityNotificationRecorder activityNotifications;
    private final EmailVerificationService emailVerification;

    public RegisterUserUseCase(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access,
            UserPreferencesRepository preferences,
            ActivityNotificationRecorder activityNotifications,
            EmailVerificationService emailVerification) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.preferences = preferences;
        this.access = access;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerification = emailVerification;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    @Audited(module = "platform", entityType = "USER", action = "CREATE")
    public UserResponse execute(RegisterUserRequest request) {
        access.lockAdministration();
        access.require(
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule.USERS,
                com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CREATE);
        access.validateAssignment(request.roleId(), request.departmentId());
        Email email = new Email(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(
                    "Email already registered: " + request.email());
        }

        UserName userName = new UserName(request.userName());
        Password.validateRawPassword(request.password());

        String hashedPassword = passwordEncoder.encode(request.password());
        Password password = Password.fromHash(hashedPassword);

        User user = new User(UserId.generate(), email, userName, password);

        user.updateProfile(request.profile(), request.roleId(),
                request.departmentId());
        User savedUser = userRepository.save(user);

        preferences.updateLanguage(savedUser.getId().getValue(),
                request.language());
        activityNotifications.record(ActivityEvent.of("USERS",
                "CREATE", "USER", savedUser.getId().getValue(),
                savedUser.getEmail().getValue(), null));
        emailVerification.requestVerification(savedUser, request.language());
        return UserResponse.from(savedUser).withLanguage(request.language());
    }
}
