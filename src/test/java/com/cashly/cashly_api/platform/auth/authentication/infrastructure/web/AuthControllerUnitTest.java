package com.cashly.cashly_api.platform.auth.authentication.infrastructure.web;

import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.register.use_case.RegisterUserUseCase;
import com.cashly.cashly_api.platform.auth.authentication.application.login.request.LoginRequest;
import com.cashly.cashly_api.platform.auth.authentication.application.login.use_case.LoginUserUseCase;
import com.cashly.cashly_api.platform.auth.token.application.refresh.request.RefreshTokenRequest;
import com.cashly.cashly_api.platform.auth.token.application.refresh.use_case.RefreshTokenUseCase;
import com.cashly.cashly_api.platform.auth.token.application.logout.request.LogoutRequest;
import com.cashly.cashly_api.platform.auth.token.application.logout.use_case.LogoutUserUseCase;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerUnitTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private LoginUserUseCase loginUserUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Mock
    private LogoutUserUseCase logoutUserUseCase;

    @Mock
    private com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository preferences;

    @InjectMocks
    private AuthController authController;

    private RegisterUserRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private LogoutRequest logoutRequest;
    private UserResponse userResponse;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterUserRequest("test@example.com",
                "Test User", "SecurePassword123!", "John", "Doe", null, null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"));

        loginRequest = new LoginRequest("test@example.com",
                "SecurePassword123!");

        refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        logoutRequest = new LogoutRequest("refresh-token");

        LocalDateTime now = LocalDateTime.now();
        userResponse = new UserResponse("user-id", "test@example.com",
                "Test User", UserRole.OPERATOR, true, false, false,
                java.util.Set.of(), "John", "Doe", null, null,
                java.util.UUID
                        .fromString("10000000-0000-0000-0000-000000000001"),
                java.util.UUID
                        .fromString("20000000-0000-0000-0000-000000000001"),
                "Custom role", "Operations", now, now);

        AuthenticatedEntityResponse entityResponse = new AuthenticatedEntityResponse(
                "user-id", "test@example.com", "Test User",
                AuthenticableType.USER, UserRole.OPERATOR, true,
                java.util.Set.of(), now, now);

        authenticationResponse = new AuthenticationResponse("access-token",
                "refresh-token", "Bearer", 900000L, entityResponse);
    }

    @Test
    @DisplayName("should_RegisterUser_When_ValidRequestProvided")
    void should_RegisterUser_When_ValidRequestProvided() {
        when(registerUserUseCase.execute(any(RegisterUserRequest.class)))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response = authController
                .register(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
        verify(registerUserUseCase, times(1)).execute(registerRequest);
    }

    @Test
    @DisplayName("should_ReturnCreatedStatus_When_RegisteringUser")
    void should_ReturnCreatedStatus_When_RegisteringUser() {
        when(registerUserUseCase.execute(any(RegisterUserRequest.class)))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response = authController
                .register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("should_LoginUser_When_ValidCredentialsProvided")
    void should_LoginUser_When_ValidCredentialsProvided() {
        when(loginUserUseCase.execute(any(LoginRequest.class)))
                .thenReturn(authenticationResponse);

        ResponseEntity<AuthenticationResponse> response = authController
                .login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticationResponse, response.getBody());
        verify(loginUserUseCase, times(1)).execute(loginRequest);
    }

    @Test
    @DisplayName("should_ReturnOkStatus_When_LoginSuccessful")
    void should_ReturnOkStatus_When_LoginSuccessful() {
        when(loginUserUseCase.execute(any(LoginRequest.class)))
                .thenReturn(authenticationResponse);

        ResponseEntity<AuthenticationResponse> response = authController
                .login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("should_RefreshToken_When_ValidRefreshTokenProvided")
    void should_RefreshToken_When_ValidRefreshTokenProvided() {
        when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class)))
                .thenReturn(authenticationResponse);

        ResponseEntity<AuthenticationResponse> response = authController
                .refresh(refreshTokenRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticationResponse, response.getBody());
        verify(refreshTokenUseCase, times(1)).execute(refreshTokenRequest);
    }

    @Test
    @DisplayName("should_ReturnOkStatus_When_RefreshSuccessful")
    void should_ReturnOkStatus_When_RefreshSuccessful() {
        when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class)))
                .thenReturn(authenticationResponse);

        ResponseEntity<AuthenticationResponse> response = authController
                .refresh(refreshTokenRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("should_LogoutUser_When_ValidRefreshTokenProvided")
    void should_LogoutUser_When_ValidRefreshTokenProvided() {
        doNothing().when(logoutUserUseCase).execute(any(LogoutRequest.class));

        ResponseEntity<Void> response = authController.logout(logoutRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(logoutUserUseCase, times(1)).execute(logoutRequest);
    }

    @Test
    @DisplayName("should_ReturnNoContentStatus_When_LogoutSuccessful")
    void should_ReturnNoContentStatus_When_LogoutSuccessful() {
        doNothing().when(logoutUserUseCase).execute(any(LogoutRequest.class));

        ResponseEntity<Void> response = authController.logout(logoutRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("should_CallRegisterUseCase_When_RegisterEndpointInvoked")
    void should_CallRegisterUseCase_When_RegisterEndpointInvoked() {
        when(registerUserUseCase.execute(any(RegisterUserRequest.class)))
                .thenReturn(userResponse);

        authController.register(registerRequest);

        verify(registerUserUseCase, times(1)).execute(registerRequest);
    }

    @Test
    @DisplayName("should_CallLoginUseCase_When_LoginEndpointInvoked")
    void should_CallLoginUseCase_When_LoginEndpointInvoked() {
        when(loginUserUseCase.execute(any(LoginRequest.class)))
                .thenReturn(authenticationResponse);

        authController.login(loginRequest);

        verify(loginUserUseCase, times(1)).execute(loginRequest);
    }

    @Test
    @DisplayName("should_CallRefreshTokenUseCase_When_RefreshEndpointInvoked")
    void should_CallRefreshTokenUseCase_When_RefreshEndpointInvoked() {
        when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class)))
                .thenReturn(authenticationResponse);

        authController.refresh(refreshTokenRequest);

        verify(refreshTokenUseCase, times(1)).execute(refreshTokenRequest);
    }

    @Test
    @DisplayName("should_CallLogoutUseCase_When_LogoutEndpointInvoked")
    void should_CallLogoutUseCase_When_LogoutEndpointInvoked() {
        doNothing().when(logoutUserUseCase).execute(any(LogoutRequest.class));

        authController.logout(logoutRequest);

        verify(logoutUserUseCase, times(1)).execute(logoutRequest);
    }
}