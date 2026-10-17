package com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.entities;

import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RefreshTokenEntity Unit Tests")
class RefreshTokenEntityUnitTest {

    private RefreshToken testRefreshToken;
    private String testSubjectId;

    @BeforeEach
    void setUp() {
        testSubjectId = UUID.randomUUID().toString();
        testRefreshToken = new RefreshToken(
            RefreshTokenId.generate(),
            testSubjectId,
            AuthenticableType.USER,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(7)
        );
    }

    @Test
    @DisplayName("should_ConvertFromDomain_When_ValidRefreshTokenProvided")
    void should_ConvertFromDomain_When_ValidRefreshTokenProvided() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        assertNotNull(entity);
        assertEquals(testRefreshToken.getId().getValue(), entity.getId());
        assertEquals(UUID.fromString(testRefreshToken.getSubjectId()), entity.getSubjectId());
        assertEquals(testRefreshToken.getSubjectType(), entity.getSubjectType());
        assertEquals(testRefreshToken.getToken(), entity.getToken());
        assertEquals(testRefreshToken.getExpiresAt(), entity.getExpiresAt());
        assertEquals(testRefreshToken.isRevoked(), entity.isRevoked());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("should_ThrowException_When_ConvertingNullRefreshToken")
    void should_ThrowException_When_ConvertingNullRefreshToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> RefreshTokenEntity.fromDomain(null)
        );

        assertEquals("RefreshToken cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_ConvertToDomain_When_ValidEntityProvided")
    void should_ConvertToDomain_When_ValidEntityProvided() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        RefreshToken domainToken = entity.toDomain();

        assertNotNull(domainToken);
        assertEquals(testRefreshToken.getId(), domainToken.getId());
        assertEquals(testRefreshToken.getSubjectId(), domainToken.getSubjectId());
        assertEquals(testRefreshToken.getSubjectType(), domainToken.getSubjectType());
        assertEquals(testRefreshToken.getToken(), domainToken.getToken());
        assertEquals(testRefreshToken.getExpiresAt(), domainToken.getExpiresAt());
        assertEquals(testRefreshToken.isRevoked(), domainToken.isRevoked());
    }

    @Test
    @DisplayName("should_UpdateFromDomain_When_ValidRefreshTokenProvided")
    void should_UpdateFromDomain_When_ValidRefreshTokenProvided() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        testRefreshToken.revoke();

        entity.updateFromDomain(testRefreshToken);

        assertTrue(entity.isRevoked());
    }

    @Test
    @DisplayName("should_ThrowException_When_UpdatingWithNullRefreshToken")
    void should_ThrowException_When_UpdatingWithNullRefreshToken() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> entity.updateFromDomain(null)
        );

        assertEquals("RefreshToken cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_PreserveImmutableFields_When_UpdateFromDomain")
    void should_PreserveImmutableFields_When_UpdateFromDomain() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);
        UUID originalId = entity.getId();
        UUID originalSubjectId = entity.getSubjectId();
        AuthenticableType originalSubjectType = entity.getSubjectType();
        String originalToken = entity.getToken();
        LocalDateTime originalExpiresAt = entity.getExpiresAt();

        RefreshToken differentToken = new RefreshToken(
            RefreshTokenId.generate(),
            UUID.randomUUID().toString(),
            AuthenticableType.USER,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(14)
        );
        differentToken.revoke();

        entity.updateFromDomain(differentToken);

        assertEquals(originalId, entity.getId());
        assertEquals(originalSubjectId, entity.getSubjectId());
        assertEquals(originalSubjectType, entity.getSubjectType());
        assertEquals(originalToken, entity.getToken());
        assertEquals(originalExpiresAt, entity.getExpiresAt());
        assertTrue(entity.isRevoked());
    }

    @Test
    @DisplayName("should_HandleRevokedToken_When_Converting")
    void should_HandleRevokedToken_When_Converting() {
        testRefreshToken.revoke();
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        RefreshToken domainToken = entity.toDomain();

        assertTrue(domainToken.isRevoked());
    }

    @Test
    @DisplayName("should_HandleExpiredToken_When_Converting")
    void should_HandleExpiredToken_When_Converting() {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setSubjectId(UUID.randomUUID());
        entity.setSubjectType(AuthenticableType.USER);
        entity.setToken(UUID.randomUUID().toString());
        entity.setExpiresAt(LocalDateTime.now().minusDays(1)); 
        entity.setRevoked(false);
        entity.setCreatedAt(LocalDateTime.now().minusDays(2));

        RefreshToken domainToken = entity.toDomain();

        assertTrue(domainToken.isExpired());
        assertFalse(domainToken.isRevoked());
    }

    @Test
    @DisplayName("should_RoundTripConversion_When_ConvertingBothWays")
    void should_RoundTripConversion_When_ConvertingBothWays() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        RefreshToken domainToken = entity.toDomain();
        RefreshTokenEntity newEntity = RefreshTokenEntity.fromDomain(domainToken);

        assertEquals(entity.getId(), newEntity.getId());
        assertEquals(entity.getSubjectId(), newEntity.getSubjectId());
        assertEquals(entity.getSubjectType(), newEntity.getSubjectType());
        assertEquals(entity.getToken(), newEntity.getToken());
        assertEquals(entity.getExpiresAt(), newEntity.getExpiresAt());
        assertEquals(entity.isRevoked(), newEntity.isRevoked());
    }

    @Test
    @DisplayName("should_HandleActiveToken_When_Converting")
    void should_HandleActiveToken_When_Converting() {
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(testRefreshToken);

        RefreshToken domainToken = entity.toDomain();

        assertFalse(domainToken.isRevoked());
        assertFalse(domainToken.isExpired());
    }

}
