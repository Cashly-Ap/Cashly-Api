package com.parsun.parsun_api.auth.user.infrastructure.web;

import com.parsun.parsun_api.auth.user.application.dto.request.UpdateUserEmailRequest;
import com.parsun.parsun_api.auth.user.application.dto.request.UpdateUserPasswordRequest;
import com.parsun.parsun_api.auth.user.application.dto.request.UpdateUserRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.application.usecases.GetAllUsersUseCase;
import com.parsun.parsun_api.auth.user.application.usecases.GetUserByIdUseCase;
import com.parsun.parsun_api.auth.user.application.usecases.RegisterUserUseCase;
import com.parsun.parsun_api.auth.user.application.usecases.UpdateUserEmailUseCase;
import com.parsun.parsun_api.auth.user.application.usecases.UpdateUserPasswordUseCase;
import com.parsun.parsun_api.auth.user.application.dto.request.RegisterUserRequest;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateUserEmailUseCase updateUserEmailUseCase;
    private final UpdateUserPasswordUseCase updateUserPasswordUseCase;
    private final UserRepository userRepository;

    public UserController(
            GetAllUsersUseCase getAllUsersUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            RegisterUserUseCase registerUserUseCase,
            UpdateUserEmailUseCase updateUserEmailUseCase,
            UpdateUserPasswordUseCase updateUserPasswordUseCase,
            UserRepository userRepository) {
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.updateUserEmailUseCase = updateUserEmailUseCase;
        this.updateUserPasswordUseCase = updateUserPasswordUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll(Authentication authentication) {
        List<UserResponse> users = getAllUsersUseCase.execute(authentication.getName());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        UserResponse user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse user = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.updateUserName(new com.parsun.parsun_api.auth.user.domain.valueobjects.UserName(request.userName()));
        user.setRole(request.role());

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
        return ResponseEntity.ok(UserResponse.from(savedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        
        // Prevent SUPER_ADMIN from deleting themselves
        if (id.equals(java.util.UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd20a215").toString())) {
            throw new IllegalArgumentException("Cannot delete the super admin user");
        }
        
        UserId userId = new UserId(UUID.fromString(id));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Prevent deletion of other SUPER_ADMIN users
        if (user.getRole() == com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot delete another SUPER_ADMIN user");
        }
        
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateEmail(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserEmailRequest request) {
        return ResponseEntity.ok(updateUserEmailUseCase.execute(id, request));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        return ResponseEntity.ok(updateUserPasswordUseCase.execute(id, request));
    }
}