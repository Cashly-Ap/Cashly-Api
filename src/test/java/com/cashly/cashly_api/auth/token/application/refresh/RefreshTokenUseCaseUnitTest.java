package com.cashly.cashly_api.auth.token.application.refresh;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.domain.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.AuthenticableType;
import com.cashly.cashly_api.auth.token.application.refresh.RefreshTokenRequest;
import com.cashly.cashly_api.auth.token.domain.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.authentication.infrastructure.integration.UserAuthenticableAdapter;
import com.cashly.cashly_api.auth.token.domain.RefreshToken;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.token.application.exceptions.InvalidTokenException;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase Unit Tests")
class RefreshTokenUseCaseUnitTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticableSubjectGateway subjectGateway;

    @Mock
    private TokenService tokenService;

    private RefreshTokenUseCase refreshTokenUseCase;

    private RefreshTokenRequest validRequest;
    private RefreshToken validRefreshToken;
    private User testUser;
    private UserId userId;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCase(
            refreshTokenRepository,
            subjectGateway,
            tokenService
        );

        String tokenString = UUID.randomUUID().toString();
        validRequest = new RefreshTokenRequest(tokenString);

        userId = UserId.generate();
        validRefreshToken = new RefreshToken(
            RefreshTokenId.generate(),
            userId.getValue().toString(),
            AuthenticableType.USER,
            tokenString,
            LocalDateTime.now().plusDays(7)
        );

        Email email = new Email("test@example.com");
        Password password = Password.fromHash("$2a$10$hashedPassword");
        testUser = new User(userId, email, new UserName("Test User"), password);
    }

    @Test
    @DisplayName("should_RefreshTokens_When_ValidRefreshTokenProvided")
    void should_RefreshTokens_When_ValidRefreshTokenProvided() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenReturn(new UserAuthenticableAdapter(testUser));
        when(tokenService.generateAccessToken(any(Authenticable.class))).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(any(Authenticable.class))).thenReturn(validRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthenticationResponse response = refreshTokenUseCase.execute(validRequest);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals(validRefreshToken.getToken(), response.refreshToken());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(subjectGateway, times(1)).resolve(userId.getValue().toString(), AuthenticableType.USER);
        verify(tokenService, times(1)).generateAccessToken(any(Authenticable.class));
        verify(tokenService, times(1)).generateRefreshToken(any(Authenticable.class));
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenNotFound")
    void should_ThrowException_When_RefreshTokenNotFound() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> refreshTokenUseCase.execute(validRequest)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verifyNoInteractions(subjectGateway);
        verify(tokenService, never()).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenExpired")
    void should_ThrowException_When_RefreshTokenExpired() {
        RefreshToken token = new RefreshToken(
            RefreshTokenId.generate(),
            userId.getValue().toString(),
            AuthenticableType.USER,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(1)
        );

        token.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(token));

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> refreshTokenUseCase.execute(validRequest)
        );

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verifyNoInteractions(subjectGateway);
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenRevoked")
    void should_ThrowException_When_RefreshTokenRevoked() {
        validRefreshToken.revoke();
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> refreshTokenUseCase.execute(validRequest)
        );

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verifyNoInteractions(subjectGateway);
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNotFound")
    void should_ThrowException_When_UserNotFound() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> refreshTokenUseCase.execute(validRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(subjectGateway, times(1)).resolve(userId.getValue().toString(), AuthenticableType.USER);
        verify(tokenService, never()).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_GenerateNewAccessToken_When_RefreshSuccessful")
    void should_GenerateNewAccessToken_When_RefreshSuccessful() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenReturn(new UserAuthenticableAdapter(testUser));
        when(tokenService.generateAccessToken(any(Authenticable.class))).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(any(Authenticable.class))).thenReturn(validRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthenticationResponse response = refreshTokenUseCase.execute(validRequest);

        assertEquals("new-access-token", response.accessToken());
        verify(tokenService, times(1)).generateAccessToken(any(Authenticable.class));
    }

    @Test
    @DisplayName("should_GenerateNewRefreshToken_When_RefreshSuccessful")
    void should_GenerateNewRefreshToken_When_RefreshSuccessful() {
        RefreshToken newRefreshToken = new RefreshToken(
            RefreshTokenId.generate(),
            userId.getValue().toString(),
            AuthenticableType.USER,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenReturn(new UserAuthenticableAdapter(testUser));
        when(tokenService.generateAccessToken(any(Authenticable.class))).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(any(Authenticable.class))).thenReturn(newRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshToken);

        AuthenticationResponse response = refreshTokenUseCase.execute(validRequest);

        assertEquals(newRefreshToken.getToken(), response.refreshToken());
        verify(tokenService, times(1)).generateRefreshToken(any(Authenticable.class));
        verify(refreshTokenRepository, times(1)).save(newRefreshToken);
    }

    @Test
    @DisplayName("should_ReturnEntityResponse_When_RefreshSuccessful")
    void should_ReturnEntityResponse_When_RefreshSuccessful() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenReturn(new UserAuthenticableAdapter(testUser));
        when(tokenService.generateAccessToken(any(Authenticable.class))).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(any(Authenticable.class))).thenReturn(validRefreshToken);
        when(tokenService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthenticationResponse response = refreshTokenUseCase.execute(validRequest);

        assertNotNull(response.entity());
        assertEquals(AuthenticableType.USER, response.entity().type());
        assertEquals("test@example.com", response.entity().email());
    }
    @Test
    void should_reject_inactive_subject_before_rotating_tokens() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
                .thenReturn(Optional.of(validRefreshToken));
        Authenticable subject = mock(Authenticable.class);
        when(subject.canAuthenticate()).thenReturn(false);
        when(subjectGateway.resolve(userId.getValue().toString(), AuthenticableType.USER)).thenReturn(subject);

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> refreshTokenUseCase.execute(validRequest));

        assertEquals("Account is not active", exception.getMessage());
        assertTrue(validRefreshToken.isValid());
        verify(refreshTokenRepository, never()).save(any());
        verifyNoInteractions(tokenService);
    }
}
