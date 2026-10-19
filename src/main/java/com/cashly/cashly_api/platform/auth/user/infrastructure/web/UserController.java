package com.cashly.cashly_api.platform.auth.user.infrastructure.web;

import com.cashly.cashly_api.platform.auth.user.application.email.request.UpdateUserEmailRequest;
import com.cashly.cashly_api.platform.auth.user.application.password.request.UpdateUserPasswordRequest;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.delete.use_case.DeleteUserUseCase;
import com.cashly.cashly_api.platform.auth.user.application.queries.use_case.GetAllUsersUseCase;
import com.cashly.cashly_api.platform.auth.user.application.queries.use_case.GetUserByIdUseCase;
import com.cashly.cashly_api.platform.auth.user.application.register.use_case.RegisterUserUseCase;
import com.cashly.cashly_api.platform.auth.user.application.email.use_case.UpdateUserEmailUseCase;
import com.cashly.cashly_api.platform.auth.user.application.password.use_case.UpdateUserPasswordUseCase;
import com.cashly.cashly_api.platform.auth.user.application.update.use_case.UpdateUserUseCase;
import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserEmailUseCase updateUserEmailUseCase;
    private final UpdateUserPasswordUseCase updateUserPasswordUseCase;

    public UserController(
            GetAllUsersUseCase getAllUsersUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            RegisterUserUseCase registerUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            UpdateUserEmailUseCase updateUserEmailUseCase,
            UpdateUserPasswordUseCase updateUserPasswordUseCase) {
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.updateUserEmailUseCase = updateUserEmailUseCase;
        this.updateUserPasswordUseCase = updateUserPasswordUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ResponseEntity<PageResponse<UserResponse>> getAll(
            Authentication authentication,
            @RequestParam(required = false) java.util.UUID roleId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userName") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        PageResponse<UserResponse> users = getAllUsersUseCase.execute(
            authentication.getName(), roleId, active, search, page, size, orderBy, orderDir);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        UserResponse user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USERS:CREATE')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse user = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:UPDATE')")
    public ResponseEntity<UserResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = updateUserUseCase.execute(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/email")
    @PreAuthorize("hasAuthority('USERS:UPDATE')")
    public ResponseEntity<UserResponse> updateEmail(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserEmailRequest request) {
        return ResponseEntity.ok(updateUserEmailUseCase.execute(id, request));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('USERS:UPDATE')")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        return ResponseEntity.ok(updateUserPasswordUseCase.execute(id, request));
    }

}
