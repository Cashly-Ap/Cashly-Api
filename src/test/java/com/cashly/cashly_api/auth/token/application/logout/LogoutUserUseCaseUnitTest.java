package com.cashly.cashly_api.auth.token.application.logout;

import com.cashly.cashly_api.auth.token.application.logout.LogoutRequest;
import com.cashly.cashly_api.auth.token.domain.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.RefreshToken;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.token.application.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUserUseCase Unit Tests")
class LogoutUserUseCaseUnitTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUserUseCase logoutUserUseCase;

    private LogoutRequest validRequest;
    private RefreshToken validRefreshToken;

    @BeforeEach
    void setUp() {
        String tokenString = UUID.randomUUID().toString();
        validRequest = new LogoutRequest(tokenString);

        validRefreshToken = new RefreshToken(
            RefreshTokenId.generate(),
            UserId.generate(),
            tokenString,
            LocalDateTime.now().plusDays(7)
        );
    }

    @Test
    @DisplayName("should_RevokeRefreshToken_When_ValidTokenProvided")
    void should_RevokeRefreshToken_When_ValidTokenProvided() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        logoutUserUseCase.execute(validRequest);

        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_RefreshTokenNotFound")
    void should_ThrowException_When_RefreshTokenNotFound() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> logoutUserUseCase.execute(validRequest)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_MarkTokenAsRevoked_When_LoggingOut")
    void should_MarkTokenAsRevoked_When_LoggingOut() {
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> {
                RefreshToken token = invocation.getArgument(0);
                assertTrue(token.isRevoked());
                return token;
            });

        logoutUserUseCase.execute(validRequest);

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_HandleAlreadyRevokedToken_When_LoggingOut")
    void should_HandleAlreadyRevokedToken_When_LoggingOut() {
        validRefreshToken.revoke();
        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        logoutUserUseCase.execute(validRequest);

        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should_HandleExpiredToken_When_LoggingOut")
    void should_HandleExpiredToken_When_LoggingOut() {
        RefreshToken token = new RefreshToken(
            RefreshTokenId.generate(),
            UserId.generate(),
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(1) 
        );

        try {
            java.lang.reflect.Field expiresAtField = RefreshToken.class.getDeclaredField("expiresAt");
            expiresAtField.setAccessible(true);
            expiresAtField.set(token, LocalDateTime.now().minusDays(1)); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(refreshTokenRepository.findByToken(validRequest.refreshToken()))
            .thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        logoutUserUseCase.execute(validRequest);

        verify(refreshTokenRepository, times(1)).findByToken(validRequest.refreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }
}
