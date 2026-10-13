package com.cashly.cashly_api.auth.authentication.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.auth.user.application.dto.request.UpdateUserPasswordRequest;
import com.cashly.cashly_api.auth.user.application.usecases.UpdateUserPasswordUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.application.dto.request.LoginRequest;
import com.cashly.cashly_api.auth.token.application.dto.request.LogoutRequest;
import com.cashly.cashly_api.auth.token.application.dto.request.RefreshTokenRequest;
import com.cashly.cashly_api.auth.user.application.dto.request.RegisterUserRequest;
import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.authentication.application.usecases.LoginUserUseCase;
import com.cashly.cashly_api.auth.token.application.usecases.LogoutUserUseCase;
import com.cashly.cashly_api.auth.token.application.usecases.RefreshTokenUseCase;
import com.cashly.cashly_api.auth.user.application.usecases.RegisterUserUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final UpdateUserPasswordUseCase updateUserPasswordUseCase;

    public AuthController(
        RegisterUserUseCase registerUserUseCase,
        LoginUserUseCase loginUserUseCase,
        RefreshTokenUseCase refreshTokenUseCase,
        LogoutUserUseCase logoutUserUseCase,
        UpdateUserPasswordUseCase updateUserPasswordUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
        this.updateUserPasswordUseCase = updateUserPasswordUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUserUseCase.execute(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        String userId = (String) authentication.getPrincipal();
        UserResponse response = updateUserPasswordUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }
}