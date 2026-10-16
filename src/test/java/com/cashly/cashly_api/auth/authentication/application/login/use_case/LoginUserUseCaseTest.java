package com.cashly.cashly_api.auth.authentication.application.login.use_case;

import com.cashly.cashly_api.auth.authentication.application.login.request.LoginRequest;
import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase Tests")
class LoginUserUseCaseTest {

    @Mock
    private AuthenticableResolver authenticableResolver;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUserUseCase useCase;

    private Authenticable testAuthenticable;
    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        testAuthenticable = new TestAuthenticable(
            "user-123",
            "test@example.com",
            "$2a$10$hashedPassword",
            "Test User",
            true
        );
        validRequest = new LoginRequest("test@example.com", "SecurePass123!");
    }

    @Test
    @DisplayName("should_AuthenticateUser_When_ValidCredentialsProvided")
    void should_AuthenticateUser_When_ValidCredentialsProvided() {
        RefreshToken refreshToken = createRefreshToken();

        when(authenticableResolver.resolveByEmail("test@example.com"))
            .thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword"))
            .thenReturn(true);
        when(tokenService.generateAccessToken(isA(Authenticable.class)))
            .thenReturn("access-token-123");
        when(tokenService.generateRefreshToken(isA(Authenticable.class)))
            .thenReturn(refreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900L);

        AuthenticationResponse response = useCase.execute(validRequest);

        assertNotNull(response);
        assertEquals("access-token-123", response.accessToken());
        assertEquals("refresh-token-123", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.expiresIn());
        assertNotNull(response.entity());

        verify(authenticableResolver).resolveByEmail("test@example.com");
        verify(passwordEncoder).matches("SecurePass123!", "$2a$10$hashedPassword");
        verify(tokenService).generateAccessToken(testAuthenticable);
        verify(tokenService).generateRefreshToken(testAuthenticable);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_ThrowInvalidCredentialsException_When_UserNotFound")
    void should_ThrowInvalidCredentialsException_When_UserNotFound() {
        when(authenticableResolver.resolveByEmail("test@example.com"))
            .thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> useCase.execute(validRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(authenticableResolver).resolveByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(isA(Authenticable.class));
    }

    @Test
    @DisplayName("should_ThrowInvalidCredentialsException_When_PasswordDoesNotMatch")
    void should_ThrowInvalidCredentialsException_When_PasswordDoesNotMatch() {
        when(authenticableResolver.resolveByEmail("test@example.com"))
            .thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPassword"))
            .thenReturn(false);

        LoginRequest wrongPasswordRequest = new LoginRequest("test@example.com", "WrongPassword");

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> useCase.execute(wrongPasswordRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder).matches("WrongPassword", "$2a$10$hashedPassword");
        verify(tokenService, never()).generateAccessToken(isA(Authenticable.class));
    }

    @Test
    @DisplayName("should_ThrowInvalidCredentialsException_When_AccountNotActive")
    void should_ThrowInvalidCredentialsException_When_AccountNotActive() {
        Authenticable inactiveAuthenticable = new TestAuthenticable(
            "user-456",
            "inactive@example.com",
            "$2a$10$hashedPassword",
            "Inactive User",
            false
        );

        when(authenticableResolver.resolveByEmail("inactive@example.com"))
            .thenReturn(Optional.of(inactiveAuthenticable));

        LoginRequest request = new LoginRequest("inactive@example.com", "SecurePass123!");

        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> useCase.execute(request)
        );

        assertEquals("Account is not active", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("should_NormalizeEmail_When_EmailHasWhitespaceOrUppercase")
    void should_NormalizeEmail_When_EmailHasWhitespaceOrUppercase() {
        RefreshToken refreshToken = createRefreshToken();

        LoginRequest request = new LoginRequest("  TEST@EXAMPLE.COM  ", "SecurePass123!");

        when(authenticableResolver.resolveByEmail("test@example.com"))
            .thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword"))
            .thenReturn(true);
        when(tokenService.generateAccessToken(isA(Authenticable.class)))
            .thenReturn("access-token-123");
        when(tokenService.generateRefreshToken(isA(Authenticable.class)))
            .thenReturn(refreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900L);

        AuthenticationResponse response = useCase.execute(request);

        assertNotNull(response);
        verify(authenticableResolver).resolveByEmail("test@example.com");
    }

    @Test
    @DisplayName("should_SaveRefreshToken_When_AuthenticationSucceeds")
    void should_SaveRefreshToken_When_AuthenticationSucceeds() {
        RefreshToken refreshToken = createRefreshToken();

        when(authenticableResolver.resolveByEmail("test@example.com"))
            .thenReturn(Optional.of(testAuthenticable));
        when(passwordEncoder.matches("SecurePass123!", "$2a$10$hashedPassword"))
            .thenReturn(true);
        when(tokenService.generateAccessToken(isA(Authenticable.class)))
            .thenReturn("access-token-123");
        when(tokenService.generateRefreshToken(isA(Authenticable.class)))
            .thenReturn(refreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900L);

        useCase.execute(validRequest);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    private RefreshToken createRefreshToken() {
        return new RefreshToken(
            RefreshTokenId.generate(),
            "user-123",
            AuthenticableType.USER,
            "refresh-token-123",
            LocalDateTime.now().plusDays(7)
        );
    }

    
    private static class TestAuthenticable implements Authenticable {
        private final String subjectId;
        private final String email;
        private final String hashedPassword;
        private final String userName;
        private final boolean active;

        TestAuthenticable(String subjectId, String email, String hashedPassword,
                          String userName, boolean active) {
            this.subjectId = subjectId;
            this.email = email;
            this.hashedPassword = hashedPassword;
            this.userName = userName;
            this.active = active;
        }

        @Override
        public String getSubjectId() { return subjectId; }

        @Override
        public String getEmail() { return email; }

        @Override
        public String getHashedPassword() { return hashedPassword; }

        @Override
        public String getUserName() { return userName; }

        @Override
        public AuthenticableType getType() { return AuthenticableType.USER; }

        @Override
        public boolean canAuthenticate() { return active; }

        @Override
        public boolean isActive() { return active; }

        @Override
        public LocalDateTime getCreatedAt() { return LocalDateTime.now(); }
    }
}
