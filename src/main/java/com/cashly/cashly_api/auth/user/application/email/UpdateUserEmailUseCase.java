package com.cashly.cashly_api.auth.user.application.email;

import com.cashly.cashly_api.auth.user.application.email.UpdateUserEmailRequest;
import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
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
