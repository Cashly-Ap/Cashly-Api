package com.cashly.cashly_api.platform.auth.token.domain.valueobjects;

import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenIdUnitTest;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenIdUnitTest {

    @Test
    void should_CreateRefreshTokenId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();

        RefreshTokenId tokenId = new RefreshTokenId(uuid);

        assertNotNull(tokenId);
        assertEquals(uuid, tokenId.getValue());
    }

    @Test
    void should_ThrowException_When_NullUuidProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new RefreshTokenId(null));
        assertEquals("RefreshToken ID cannot be null", exception.getMessage());
    }

    @Test
    void should_GenerateRandomRefreshTokenId_When_GenerateMethodCalled() {
        RefreshTokenId tokenId1 = RefreshTokenId.generate();
        RefreshTokenId tokenId2 = RefreshTokenId.generate();

        assertNotNull(tokenId1);
        assertNotNull(tokenId2);
        assertNotEquals(tokenId1, tokenId2);
    }

    @Test
    void should_CreateRefreshTokenIdFromString_When_ValidUuidStringProvided() {
        String uuidString = "660e8400-e29b-41d4-a716-446655440000";

        RefreshTokenId tokenId = RefreshTokenId.from(uuidString);

        assertNotNull(tokenId);
        assertEquals(UUID.fromString(uuidString), tokenId.getValue());
    }

    @Test
    void should_ThrowException_When_InvalidUuidStringProvided() {
        String invalidUuid = "invalid-token-id";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RefreshTokenId.from(invalidUuid));
        assertTrue(exception.getMessage()
                .contains("Invalid RefreshToken ID format"));
    }

    @Test
    void should_ThrowException_When_NullStringProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RefreshTokenId.from(null));
        assertEquals("RefreshToken ID string cannot be null or empty",
                exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyStringProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RefreshTokenId.from("   "));
        assertEquals("RefreshToken ID string cannot be null or empty",
                exception.getMessage());
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        RefreshTokenId tokenId1 = new RefreshTokenId(uuid);
        RefreshTokenId tokenId2 = new RefreshTokenId(uuid);

        assertEquals(tokenId1, tokenId2);
        assertEquals(tokenId1.hashCode(), tokenId2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidProvided() {
        RefreshTokenId tokenId1 = RefreshTokenId.generate();
        RefreshTokenId tokenId2 = RefreshTokenId.generate();

        assertNotEquals(tokenId1, tokenId2);
    }

    @Test
    void should_HaveToString_When_RefreshTokenIdCreated() {
        UUID uuid = UUID.randomUUID();
        RefreshTokenId tokenId = new RefreshTokenId(uuid);

        String result = tokenId.toString();

        assertNotNull(result);
        assertTrue(result.contains("RefreshTokenId"));
        assertTrue(result.contains(uuid.toString()));
    }
}
