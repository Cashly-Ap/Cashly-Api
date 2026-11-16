package com.cashly.cashly_api.platform.auth.token.domain.entity;

import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenUnitTest {

    private RefreshTokenId tokenId;
    private UserId userId;
    private String token;
    private LocalDateTime futureExpiration;

    @BeforeEach
    void setUp() {
        tokenId = RefreshTokenId.generate();
        userId = UserId.generate();
        token = "sample-refresh-token-value";
        futureExpiration = LocalDateTime.now().plusDays(7);
    }

    @Test
    void should_CreateRefreshToken_When_ValidParametersProvided() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        assertNotNull(refreshToken);
        assertEquals(tokenId, refreshToken.getId());
        assertEquals(userId, refreshToken.getUserId());
        assertEquals(token, refreshToken.getToken());
        assertEquals(futureExpiration, refreshToken.getExpiresAt());
        assertFalse(refreshToken.isRevoked());
        assertNotNull(refreshToken.getCreatedAt());
    }

    @Test
    void should_ThrowException_When_NullTokenIdProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshToken(null, userId, token, futureExpiration));
        assertEquals("RefreshToken ID cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NullUserIdProvided() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RefreshToken(tokenId, null, token, futureExpiration));
    }

    @Test
    void should_ThrowException_When_NullTokenProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new RefreshToken(tokenId,
                        userId, null, futureExpiration));
        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyTokenProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new RefreshToken(tokenId,
                        userId, "   ", futureExpiration));
        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NullExpirationProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshToken(tokenId, userId, token, null));
        assertEquals("Expiration date cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_PastExpirationProvided() {
        LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshToken(tokenId, userId, token, pastExpiration));
        assertEquals("Expiration date cannot be in the past",
                exception.getMessage());
    }

    @Test
    void should_ReturnFalse_When_TokenIsNotExpired() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        boolean isExpired = refreshToken.isExpired();

        assertFalse(isExpired);
    }

    @Test
    void should_ReturnTrue_When_TokenIsExpired() {
        LocalDateTime nearExpiration = LocalDateTime.now().plusSeconds(1);
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                nearExpiration);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isExpired = refreshToken.isExpired();

        assertTrue(isExpired);
    }

    @Test
    void should_ReturnTrue_When_TokenIsValid() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        boolean isValid = refreshToken.isValid();

        assertTrue(isValid);
    }

    @Test
    void should_ReturnFalse_When_TokenIsRevoked() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);
        refreshToken.revoke();

        boolean isValid = refreshToken.isValid();

        assertFalse(isValid);
    }

    @Test
    void should_RevokeToken_When_RevokeMethodCalled() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        refreshToken.revoke();

        assertTrue(refreshToken.isRevoked());
    }

    @Test
    void should_ReturnTrue_When_TokenBelongsToUser() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        boolean belongsToUser = refreshToken.belongsToUser(userId);

        assertTrue(belongsToUser);
    }

    @Test
    void should_ReturnFalse_When_TokenDoesNotBelongToUser() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);
        UserId differentUserId = UserId.generate();

        boolean belongsToUser = refreshToken.belongsToUser(differentUserId);

        assertFalse(belongsToUser);
    }

    @Test
    void should_ReturnFalse_When_NullUserIdProvidedForBelongsToUser() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        boolean belongsToUser = refreshToken.belongsToUser(null);

        assertFalse(belongsToUser);
    }

    @Test
    void should_BeEqual_When_SameTokenIdProvided() {
        RefreshToken token1 = new RefreshToken(tokenId, userId, token,
                futureExpiration);
        RefreshToken token2 = new RefreshToken(tokenId, UserId.generate(),
                "different", futureExpiration);

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentTokenIdProvided() {
        RefreshToken token1 = new RefreshToken(RefreshTokenId.generate(),
                userId, token, futureExpiration);
        RefreshToken token2 = new RefreshToken(RefreshTokenId.generate(),
                userId, token, futureExpiration);

        assertNotEquals(token1, token2);
    }

    @Test
    void should_HideTokenInToString_When_RefreshTokenCreated() {
        RefreshToken refreshToken = new RefreshToken(tokenId, userId, token,
                futureExpiration);

        String result = refreshToken.toString();

        assertNotNull(result);
        assertTrue(result.contains("RefreshToken"));
        assertTrue(result.contains("token='***'"));
        assertFalse(result.contains("sample-refresh-token-value"));
    }
}
