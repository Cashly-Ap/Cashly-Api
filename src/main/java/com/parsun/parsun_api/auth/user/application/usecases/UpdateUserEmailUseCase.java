package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.UpdateUserEmailRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.application.exceptions.DuplicateEmailException;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserEmailUseCase {

    private final UserRepository userRepository;

    public UpdateUserEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse execute(String id, UpdateUserEmailRequest request) {
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        Email newEmail = new Email(request.email());

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new DuplicateEmailException("Email already in use: " + request.email());
        }

        user.updateEmail(newEmail);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
