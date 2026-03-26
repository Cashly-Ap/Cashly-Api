package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.RegisterUserRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.application.exceptions.DuplicateEmailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse execute(RegisterUserRequest request) {
        Email email = new Email(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered: " + request.email());
        }

        UserName userName = new UserName(request.userName());
        Password.validateRawPassword(request.password());

        String hashedPassword = passwordEncoder.encode(request.password());
        Password password = Password.fromHash(hashedPassword);

        User user = new User(UserId.generate(), email, userName, password, request.role());

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}
