package com.cashly.cashly_api.auth.user.domain.valueobjects;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserIdUnitTest;


import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdUnitTest {

    @Test
    void should_CreateUserId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();

        UserId userId = new UserId(uuid);

        assertNotNull(userId);
        assertEquals(uuid, userId.getValue());
    }

    @Test
    void should_ThrowException_When_NullUuidProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UserId(null)
        );
        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void should_GenerateRandomUserId_When_GenerateMethodCalled() {
        UserId userId1 = UserId.generate();
        UserId userId2 = UserId.generate();

        assertNotNull(userId1);
        assertNotNull(userId2);
        assertNotEquals(userId1, userId2);
    }

    @Test
    void should_CreateUserIdFromString_When_ValidUuidStringProvided() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";

        UserId userId = UserId.from(uuidString);

        assertNotNull(userId);
        assertEquals(UUID.fromString(uuidString), userId.getValue());
    }

    @Test
    void should_ThrowException_When_InvalidUuidStringProvided() {
        String invalidUuid = "invalid-uuid";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UserId.from(invalidUuid)
        );
        assertTrue(exception.getMessage().contains("Invalid User ID format"));
    }

    @Test
    void should_ThrowException_When_NullStringProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UserId.from(null)
        );
        assertEquals("User ID string cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_EmptyStringProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UserId.from("   ")
        );
        assertEquals("User ID string cannot be null or empty", exception.getMessage());
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        UserId userId1 = new UserId(uuid);
        UserId userId2 = new UserId(uuid);

        assertEquals(userId1, userId2);
        assertEquals(userId1.hashCode(), userId2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidProvided() {
        UserId userId1 = UserId.generate();
        UserId userId2 = UserId.generate();

        assertNotEquals(userId1, userId2);
    }

    @Test
    void should_HaveToString_When_UserIdCreated() {
        UUID uuid = UUID.randomUUID();
        UserId userId = new UserId(uuid);

        String result = userId.toString();

        assertNotNull(result);
        assertTrue(result.contains("UserId"));
        assertTrue(result.contains(uuid.toString()));
    }
}
