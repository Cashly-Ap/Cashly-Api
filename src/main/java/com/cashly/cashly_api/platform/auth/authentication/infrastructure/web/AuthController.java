package com.cashly.cashly_api.platform.auth.authentication.infrastructure.web;

import com.cashly.cashly_api.platform.auth.user.application.password.request.UpdateUserPasswordRequest;
import com.cashly.cashly_api.platform.auth.user.application.password.use_case.UpdateUserPasswordUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateOwnProfileRequest;
import com.cashly.cashly_api.platform.auth.user.application.update.use_case.UpdateOwnProfileUseCase;
import com.cashly.cashly_api.platform.auth.user.application.queries.use_case.GetUserByIdUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.login.request.LoginRequest;
import com.cashly.cashly_api.platform.auth.token.application.logout.request.LogoutRequest;
import com.cashly.cashly_api.platform.auth.token.application.refresh.request.RefreshTokenRequest;
import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.login.use_case.LoginUserUseCase;
import com.cashly.cashly_api.platform.auth.token.application.logout.use_case.LogoutUserUseCase;
import com.cashly.cashly_api.platform.auth.token.application.refresh.use_case.RefreshTokenUseCase;
import com.cashly.cashly_api.platform.auth.user.application.register.use_case.RegisterUserUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UpdateOwnProfileUseCase updateOwnProfileUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final UpdateUserPasswordUseCase updateUserPasswordUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUserUseCase logoutUserUseCase,
            UpdateUserPasswordUseCase updateUserPasswordUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateOwnProfileUseCase updateOwnProfileUseCase) {
        this.updateOwnProfileUseCase = updateOwnProfileUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
        this.updateUserPasswordUseCase = updateUserPasswordUseCase;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(Authentication authentication) {
        return ResponseEntity
                .ok(getUserByIdUseCase.execute(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateOwnProfileRequest request) {
        return ResponseEntity.ok(updateOwnProfileUseCase
                .execute(authentication.getName(), request));
    }

    @PostMapping("/register")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('USERS_CREATE')")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request) {
        logoutUserUseCase.execute(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        String userId = (String) authentication.getPrincipal();
        UserResponse response = updateUserPasswordUseCase.executeSelf(userId,
                request);
        return ResponseEntity.ok(response);
    }
}