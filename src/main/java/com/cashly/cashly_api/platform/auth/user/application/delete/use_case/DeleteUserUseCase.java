package com.cashly.cashly_api.platform.auth.user.application.delete.use_case;

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

    private static final UUID SUPER_ADMIN_USER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd20a215");

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(String id) {
        if (id.equals(SUPER_ADMIN_USER_ID.toString())) {
            throw new IllegalArgumentException("Cannot delete the super admin user");
        }

        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (user.isSuperAdmin()) {
            throw new IllegalArgumentException("Cannot delete another SUPER_ADMIN user");
        }

        if (userRepository.hasAssociatedRecords(userId)) {
            throw new UserHasAssociatedRecordsException(id);
        }

        userRepository.delete(user);
    }
}
