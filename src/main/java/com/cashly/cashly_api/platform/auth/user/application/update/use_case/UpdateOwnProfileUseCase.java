package com.cashly.cashly_api.platform.auth.user.application.update.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateOwnProfileRequest;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOwnProfileUseCase {
    private final UserRepository users;
    private final AccessService access;

    public UpdateOwnProfileUseCase(UserRepository users, AccessService access) {
        this.users = users;
        this.access = access;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse execute(String authenticatedUserId, UpdateOwnProfileRequest request) {
        // Serialize with administrative writes before loading the account so assignments cannot be overwritten.
        access.lockAdministration();
        var user = users.findById(UserId.from(authenticatedUserId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.updateUserName(new UserName(request.userName()));
        user.updatePersonalProfile(request.profile());
        return UserResponse.from(users.save(user));
    }
}
