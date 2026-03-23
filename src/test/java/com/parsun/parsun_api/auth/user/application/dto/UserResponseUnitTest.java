package com.parsun.parsun_api.auth.user.application.dto;

import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserResponse Unit Tests")
class UserResponseUnitTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        UserId userId = UserId.generate();
        Email email = new Email("test@example.com");
        UserName userName = new UserName("Test User");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        testUser = new User(userId, email, userName, password);
    }

    @Test
    @DisplayName("should create UserResponse from User")
    void shouldCreateUserResponseFromUser() {
        UserResponse response = UserResponse.from(testUser);

        assertNotNull(response);
        assertEquals(testUser.getId().getValue().toString(), response.id());
        assertEquals(testUser.getEmail().getValue(), response.email());
        assertEquals(testUser.getUserName().getValue(), response.displayName());
        assertTrue(response.active());
        assertFalse(response.emailVerified());
        assertNotNull(response.createdAt());
    }

    @Test
    @DisplayName("should handle User with minimal information")
    void shouldHandleUserWithMinimalInformation() {
        UserId userId = UserId.generate();
        Email email = new Email("minimal@example.com");
        UserName userName = new UserName("Min");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        User minimalUser = new User(userId, email, userName, password);

        UserResponse response = UserResponse.from(minimalUser);

        assertNotNull(response);
        assertEquals(userId.getValue().toString(), response.id());
        assertEquals("minimal@example.com", response.email());
        assertEquals("Min", response.displayName());
    }

    @Test
    @DisplayName("should preserve User ID as string")
    void shouldPreserveUserIdAsString() {
        UserResponse response = UserResponse.from(testUser);

        assertNotNull(response.id());
        assertFalse(response.id().isEmpty());
        assertTrue(response.id().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @DisplayName("should preserve email correctly")
    void shouldPreserveEmailCorrectly() {
        UserResponse response = UserResponse.from(testUser);

        assertEquals("test@example.com", response.email());
    }

    @Test
    @DisplayName("should preserve userName correctly")
    void shouldPreserveUserNameCorrectly() {
        UserResponse response = UserResponse.from(testUser);

        assertEquals("Test User", response.displayName());
    }

    @Test
    @DisplayName("should include createdAt timestamp")
    void shouldIncludeCreatedAtTimestamp() {
        UserResponse response = UserResponse.from(testUser);

        assertNotNull(response.createdAt());
    }

    @Test
    @DisplayName("should create different responses for different users")
    void shouldCreateDifferentResponsesForDifferentUsers() {
        UserId userId2 = UserId.generate();
        Email email2 = new Email("another@example.com");
        UserName userName2 = new UserName("Another User");
        Password password2 = Password.fromHash("$2a$10$anotherHashedPassword");

        User anotherUser = new User(userId2, email2, userName2, password2);

        UserResponse response1 = UserResponse.from(testUser);
        UserResponse response2 = UserResponse.from(anotherUser);

        assertNotEquals(response1.id(), response2.id());
        assertNotEquals(response1.email(), response2.email());
        assertNotEquals(response1.displayName(), response2.displayName());
    }

    @Test
    @DisplayName("should maintain data consistency across multiple calls")
    void shouldMaintainDataConsistencyAcrossMultipleCalls() {
        UserResponse response1 = UserResponse.from(testUser);
        UserResponse response2 = UserResponse.from(testUser);

        assertEquals(response1.id(), response2.id());
        assertEquals(response1.email(), response2.email());
        assertEquals(response1.displayName(), response2.displayName());
        assertEquals(response1.createdAt(), response2.createdAt());
    }

    @Test
    @DisplayName("should handle userName with special characters")
    void shouldHandleUserNameWithSpecialCharacters() {
        UserId userId = UserId.generate();
        Email email = new Email("special@example.com");
        UserName userName = new UserName("Jöhn Dôe-Smith");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        User specialUser = new User(userId, email, userName, password);

        UserResponse response = UserResponse.from(specialUser);

        assertEquals("Jöhn Dôe-Smith", response.displayName());
    }

    @Test
    @DisplayName("should handle email with various valid formats")
    void shouldHandleEmailWithVariousValidFormats() {
        UserId userId = UserId.generate();
        Email email = new Email("user.name+tag@example.co.uk");
        UserName userName = new UserName("User");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        User user = new User(userId, email, userName, password);

        UserResponse response = UserResponse.from(user);

        assertEquals("user.name+tag@example.co.uk", response.email());
    }

    @Test
    @DisplayName("should include active status")
    void shouldIncludeActiveStatus() {
        UserResponse response = UserResponse.from(testUser);

        assertTrue(response.active());
    }

    @Test
    @DisplayName("should include emailVerified status")
    void shouldIncludeEmailVerifiedStatus() {
        UserResponse response = UserResponse.from(testUser);

        assertFalse(response.emailVerified());
    }
}
