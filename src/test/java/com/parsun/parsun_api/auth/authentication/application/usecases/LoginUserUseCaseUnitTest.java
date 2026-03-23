package com.parsun.parsun_api.auth.authentication.application.usecases;

import com.parsun.parsun_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.parsun.parsun_api.auth.authentication.application.dto.request.LoginRequest;
import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.parsun.parsun_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase Unit Tests")
class LoginUserUseCaseUnitTest {

    @Mock
    private AuthenticableResolver authenticableResolver;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LoginUserUseCase loginUserUseCase;

    private LoginRequest validLoginRequest;
    private Authenticable testAuthenticable;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        loginUserUseCase = new LoginUserUseCase(
            authenticableResolver,
            refreshTokenRepository,
            tokenService,
            passwordEncoder
        );

        validLoginRequest = new LoginRequest("test@example.com", "SecurePass123!");

        String userId = UUID.randomUUID().toString();
        testAuthenticable = createMockAuthenticable(userId, "test@example.com", "Test User", true);

        testRefreshToken = new RefreshToken(
            RefreshTokenId.generate(),
            userId,
            AuthenticableType.USER,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(7)
        );
    }

    private Authenticable createMockAuthenticable(String id, String email, String displayName, boolean active) {
        Authenticable auth = mock(Authenticable.class, withSettings().lenient());
        when(auth.getSubjectId()).thenReturn(id);
        when(auth.getEmail()).thenReturn(email);
        when(auth.getDisplayName()).thenReturn(displayName);
        when(auth.getType()).thenReturn(AuthenticableType.USER);
        when(auth.isActive()).thenReturn(active);
        when(auth.canAuthenticate()).thenReturn(active);
        when(auth.getHashedPassword()).thenReturn("$2a$10$hashedPassword");
        when(auth.getCreatedAt()).thenReturn(LocalDateTime.now());
        return auth;
    }

    @Test
    @DisplayName("should_AuthenticateUser_When_ValidCredentialsProvided")
    void should_AuthenticateUser_When_ValidCredentialsProvided() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthenticationResponse response = loginUserUseCase.execute(validLoginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals(testRefreshToken.getToken(), response.refreshToken());
        assertEquals(900000L, response.expiresIn());
        assertNotNull(response.entity());
        assertEquals("test@example.com", response.entity().email());
        assertEquals(AuthenticableType.USER, response.entity().type());

        verify(authenticableResolver, times(1)).resolveByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(tokenService, times(1)).generateAccessToken(testAuthenticable);
        verify(tokenService, times(1)).generateRefreshToken(testAuthenticable);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNotFound")
    void should_ThrowException_When_UserNotFound() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> loginUserUseCase.execute(validLoginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(authenticableResolver, times(1)).resolveByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_AccountIsNotActive")
    void should_ThrowException_When_AccountIsNotActive() {
        Authenticable inactiveAuth = createMockAuthenticable(
            UUID.randomUUID().toString(), "test@example.com", "Test User", false
        );
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(inactiveAuth));

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> loginUserUseCase.execute(validLoginRequest)
        );

        assertEquals("Account is not active", exception.getMessage());
        verify(authenticableResolver, times(1)).resolveByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_PasswordIsInvalid")
    void should_ThrowException_When_PasswordIsInvalid() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> loginUserUseCase.execute(validLoginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(authenticableResolver, times(1)).resolveByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_GenerateAccessToken_When_LoginSuccessful")
    void should_GenerateAccessToken_When_LoginSuccessful() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthenticationResponse response = loginUserUseCase.execute(validLoginRequest);

        assertEquals("access-token", response.accessToken());
        verify(tokenService, times(1)).generateAccessToken(testAuthenticable);
    }

    @Test
    @DisplayName("should_GenerateRefreshToken_When_LoginSuccessful")
    void should_GenerateRefreshToken_When_LoginSuccessful() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthenticationResponse response = loginUserUseCase.execute(validLoginRequest);

        assertEquals(testRefreshToken.getToken(), response.refreshToken());
        verify(tokenService, times(1)).generateRefreshToken(testAuthenticable);
        verify(refreshTokenRepository, times(1)).save(testRefreshToken);
    }

    @Test
    @DisplayName("should_SaveRefreshToken_When_LoginSuccessful")
    void should_SaveRefreshToken_When_LoginSuccessful() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        loginUserUseCase.execute(validLoginRequest);

        verify(refreshTokenRepository, times(1)).save(testRefreshToken);
    }

    @Test
    @DisplayName("should_ReturnEntityData_When_LoginSuccessful")
    void should_ReturnEntityData_When_LoginSuccessful() {
        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthenticationResponse response = loginUserUseCase.execute(validLoginRequest);

        assertNotNull(response.entity());
        assertEquals("test@example.com", response.entity().email());
    }

    @Test
    @DisplayName("should_NormalizeEmail_When_Authenticating")
    void should_NormalizeEmail_When_Authenticating() {
        LoginRequest upperCaseRequest = new LoginRequest("TEST@EXAMPLE.COM", "SecurePass123!");

        when(authenticableResolver.resolveByEmail("test@example.com")).thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(testAuthenticable)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(testAuthenticable)).thenReturn(testRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthenticationResponse response = loginUserUseCase.execute(upperCaseRequest);

        assertNotNull(response);
        verify(authenticableResolver, times(1)).resolveByEmail("test@example.com");
    }
}
