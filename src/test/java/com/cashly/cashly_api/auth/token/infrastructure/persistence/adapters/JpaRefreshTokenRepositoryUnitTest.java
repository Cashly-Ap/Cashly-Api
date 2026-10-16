package com.cashly.cashly_api.auth.token.infrastructure.persistence.adapters;


import com.cashly.cashly_api.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.auth.token.infrastructure.persistence.entities.RefreshTokenEntity;
import com.cashly.cashly_api.auth.token.infrastructure.persistence.adapters.JpaRefreshTokenRepository;
import com.cashly.cashly_api.auth.token.infrastructure.persistence.repositories.SpringDataRefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cashly.cashly_api.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaRefreshTokenRepository Unit Tests")
class JpaRefreshTokenRepositoryUnitTest {

    @Mock
    private SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    @InjectMocks
    private JpaRefreshTokenRepository jpaRefreshTokenRepository;

    private RefreshToken testRefreshToken;
    private RefreshTokenEntity testRefreshTokenEntity;
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

        testRefreshTokenEntity = RefreshTokenEntity.fromDomain(testRefreshToken);
    }

    @Test
    @DisplayName("should_SaveRefreshToken_When_NewTokenProvided")
    void should_SaveRefreshToken_When_NewTokenProvided() {
        when(springDataRefreshTokenRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(springDataRefreshTokenRepository.save(any(RefreshTokenEntity.class)))
            .thenReturn(testRefreshTokenEntity);

        RefreshToken savedToken = jpaRefreshTokenRepository.save(testRefreshToken);

        assertNotNull(savedToken);
        assertEquals(testRefreshToken.getId(), savedToken.getId());
        assertEquals(testRefreshToken.getToken(), savedToken.getToken());
        verify(springDataRefreshTokenRepository, times(1)).findById(any(UUID.class));
        verify(springDataRefreshTokenRepository, times(1)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("should_UpdateRefreshToken_When_ExistingTokenProvided")
    void should_UpdateRefreshToken_When_ExistingTokenProvided() {
        when(springDataRefreshTokenRepository.findById(any(UUID.class)))
            .thenReturn(Optional.of(testRefreshTokenEntity));
        when(springDataRefreshTokenRepository.save(any(RefreshTokenEntity.class)))
            .thenReturn(testRefreshTokenEntity);

        RefreshToken savedToken = jpaRefreshTokenRepository.save(testRefreshToken);

        assertNotNull(savedToken);
        verify(springDataRefreshTokenRepository, times(1)).findById(any(UUID.class));
        verify(springDataRefreshTokenRepository, times(1)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_SavingNullRefreshToken")
    void should_ThrowException_When_SavingNullRefreshToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaRefreshTokenRepository.save(null)
        );

        assertEquals("RefreshToken cannot be null", exception.getMessage());
        verify(springDataRefreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_FindTokenByString_When_TokenExists")
    void should_FindTokenByString_When_TokenExists() {
        String tokenString = testRefreshToken.getToken();
        when(springDataRefreshTokenRepository.findByToken(tokenString))
            .thenReturn(Optional.of(testRefreshTokenEntity));

        Optional<RefreshToken> foundToken = jpaRefreshTokenRepository.findByToken(tokenString);

        assertTrue(foundToken.isPresent());
        assertEquals(testRefreshToken.getToken(), foundToken.get().getToken());
        verify(springDataRefreshTokenRepository, times(1)).findByToken(anyString());
    }

    @Test
    @DisplayName("should_ReturnEmpty_When_TokenNotFound")
    void should_ReturnEmpty_When_TokenNotFound() {
        String tokenString = "nonexistent-token";
        when(springDataRefreshTokenRepository.findByToken(tokenString))
            .thenReturn(Optional.empty());

        Optional<RefreshToken> foundToken = jpaRefreshTokenRepository.findByToken(tokenString);

        assertFalse(foundToken.isPresent());
        verify(springDataRefreshTokenRepository, times(1)).findByToken(anyString());
    }

    @Test
    @DisplayName("should_ThrowException_When_FindingByNullToken")
    void should_ThrowException_When_FindingByNullToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaRefreshTokenRepository.findByToken(null)
        );

        assertEquals("Token cannot be null or empty", exception.getMessage());
        verify(springDataRefreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    @DisplayName("should_ThrowException_When_FindingByEmptyToken")
    void should_ThrowException_When_FindingByEmptyToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaRefreshTokenRepository.findByToken("")
        );

        assertEquals("Token cannot be null or empty", exception.getMessage());
        verify(springDataRefreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    @DisplayName("should_FindTokensByUserId_When_TokensExist")
    void should_FindTokensByUserId_When_TokensExist() {
        UserId userId = UserId.from(testSubjectId);
        
        RefreshTokenEntity entity1 = testRefreshTokenEntity;
        RefreshTokenEntity entity2 = RefreshTokenEntity.fromDomain(
            new RefreshToken(
                RefreshTokenId.generate(),
                testSubjectId,
                AuthenticableType.USER,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)
            )
        );

        when(springDataRefreshTokenRepository.findBySubjectId(any(UUID.class)))
            .thenReturn(Arrays.asList(entity1, entity2));

        List<RefreshToken> tokens = jpaRefreshTokenRepository.findByUserId(userId);

        assertEquals(2, tokens.size());
        verify(springDataRefreshTokenRepository, times(1)).findBySubjectId(any(UUID.class));
    }

    @Test
    @DisplayName("should_ReturnEmptyList_When_NoTokensForUser")
    void should_ReturnEmptyList_When_NoTokensForUser() {
        UserId userId = UserId.generate();
        when(springDataRefreshTokenRepository.findBySubjectId(userId.getValue()))
            .thenReturn(List.of());

        List<RefreshToken> tokens = jpaRefreshTokenRepository.findByUserId(userId);

        assertTrue(tokens.isEmpty());
        verify(springDataRefreshTokenRepository, times(1)).findBySubjectId(any(UUID.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_FindingByNullUserId")
    void should_ThrowException_When_FindingByNullUserId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaRefreshTokenRepository.findByUserId(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(springDataRefreshTokenRepository, never()).findBySubjectId(any(UUID.class));
    }

    @Test
    @DisplayName("should_RevokeAllTokensByUserId_When_TokensExist")
    void should_RevokeAllTokensByUserId_When_TokensExist() {
        UserId userId = UserId.from(testSubjectId);
        UUID userUuid = UUID.fromString(testSubjectId);

        RefreshTokenEntity token1 = testRefreshTokenEntity;
        RefreshTokenEntity token2 = RefreshTokenEntity.fromDomain(
            new RefreshToken(
                RefreshTokenId.generate(),
                testSubjectId,
                AuthenticableType.USER,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)
            )
        );

        when(springDataRefreshTokenRepository.findBySubjectIdAndRevoked(userUuid, false))
            .thenReturn(Arrays.asList(token1, token2));

        jpaRefreshTokenRepository.revokeAllByUserId(userId);

        verify(springDataRefreshTokenRepository, times(1))
            .findBySubjectIdAndRevoked(userUuid, false);
        verify(springDataRefreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
    }

    @Test
    @DisplayName("should_NotRevokeAnyTokens_When_NoTokensForUser")
    void should_NotRevokeAnyTokens_When_NoTokensForUser() {
        UserId userId = UserId.from(testSubjectId);
        UUID userUuid = UUID.fromString(testSubjectId);

        when(springDataRefreshTokenRepository.findBySubjectIdAndRevoked(userUuid, false))
            .thenReturn(List.of());

        jpaRefreshTokenRepository.revokeAllByUserId(userId);

        verify(springDataRefreshTokenRepository, times(1))
            .findBySubjectIdAndRevoked(userUuid, false);
        verify(springDataRefreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_RevokingByNullUserId")
    void should_ThrowException_When_RevokingByNullUserId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jpaRefreshTokenRepository.revokeAllByUserId(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(springDataRefreshTokenRepository, never())
            .findBySubjectIdAndRevoked(any(UUID.class), anyBoolean());
    }

    @Test
    @DisplayName("should_DeleteOnlyExpiredTokens_When_MixedTokensExist")
    void should_DeleteOnlyExpiredTokens_When_MixedTokensExist() {
        RefreshTokenEntity expiredToken1 = createExpiredTokenEntity();
        RefreshTokenEntity expiredToken2 = createExpiredTokenEntity();
        RefreshTokenEntity validToken = testRefreshTokenEntity;

        when(springDataRefreshTokenRepository.findAll())
            .thenReturn(Arrays.asList(expiredToken1, validToken, expiredToken2));

        jpaRefreshTokenRepository.deleteExpiredTokens();

        verify(springDataRefreshTokenRepository, times(1)).findAll();
        verify(springDataRefreshTokenRepository, times(1)).delete(expiredToken1);
        verify(springDataRefreshTokenRepository, times(1)).delete(expiredToken2);
        verify(springDataRefreshTokenRepository, never()).delete(validToken);
    }

    @Test
    @DisplayName("should_NotDeleteAnyTokens_When_AllTokensAreValid")
    void should_NotDeleteAnyTokens_When_AllTokensAreValid() {
        RefreshTokenEntity validToken1 = testRefreshTokenEntity;
        RefreshTokenEntity validToken2 = RefreshTokenEntity.fromDomain(
            new RefreshToken(
                RefreshTokenId.generate(),
                testSubjectId,
                AuthenticableType.USER,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)
            )
        );

        when(springDataRefreshTokenRepository.findAll())
            .thenReturn(Arrays.asList(validToken1, validToken2));

        jpaRefreshTokenRepository.deleteExpiredTokens();

        verify(springDataRefreshTokenRepository, times(1)).findAll();
        verify(springDataRefreshTokenRepository, never()).delete(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("should_DeleteAllTokens_When_AllTokensAreExpired")
    void should_DeleteAllTokens_When_AllTokensAreExpired() {
        RefreshTokenEntity expiredToken1 = createExpiredTokenEntity();
        RefreshTokenEntity expiredToken2 = createExpiredTokenEntity();

        when(springDataRefreshTokenRepository.findAll())
            .thenReturn(Arrays.asList(expiredToken1, expiredToken2));

        jpaRefreshTokenRepository.deleteExpiredTokens();

        verify(springDataRefreshTokenRepository, times(1)).findAll();
        verify(springDataRefreshTokenRepository, times(1)).delete(expiredToken1);
        verify(springDataRefreshTokenRepository, times(1)).delete(expiredToken2);
    }

    private RefreshTokenEntity createExpiredTokenEntity() {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setSubjectId(UUID.fromString(testSubjectId));
        entity.setSubjectType(AuthenticableType.USER);
        entity.setToken(UUID.randomUUID().toString());
        entity.setExpiresAt(LocalDateTime.now().minusDays(1));
        entity.setRevoked(false);
        entity.setCreatedAt(LocalDateTime.now().minusDays(7));
        return entity;
    }
}
