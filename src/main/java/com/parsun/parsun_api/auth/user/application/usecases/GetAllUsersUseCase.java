package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAllUsersUseCase {

    private final UserRepository userRepository;

    public GetAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> execute(String authenticatedUserId, UserRole role, Boolean active) {
        UserId excludedId = new UserId(UUID.fromString(authenticatedUserId));
        List<User> users = userRepository.findAll();
        return users.stream()
            .filter(user -> !user.getId().equals(excludedId))
            .filter(user -> role == null || user.getRole() == role)
            .filter(user -> active == null || user.isActive() == active)
            .map(UserResponse::from)
            .toList();
    }
}
