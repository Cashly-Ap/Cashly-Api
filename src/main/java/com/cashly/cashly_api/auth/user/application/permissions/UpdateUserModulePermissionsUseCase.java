package com.cashly.cashly_api.auth.user.application.permissions;

import com.cashly.cashly_api.auth.user.application.permissions.UpdateUserModulePermissionsRequest;
import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserModulePermissionsUseCase {

    private final UserRepository userRepository;

    public UpdateUserModulePermissionsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse execute(String userId, UpdateUserModulePermissionsRequest request) {
        UserId id = new UserId(UUID.fromString(userId));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setModulePermissions(request.modulePermissions());

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}
