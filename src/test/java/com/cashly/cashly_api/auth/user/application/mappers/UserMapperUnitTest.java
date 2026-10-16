package com.cashly.cashly_api.auth.user.application.mappers;

import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Unit Tests")
class UserMapperUnitTest {

    private UserMapper userMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        UserId userId = UserId.generate();
        Email email = new Email("test@example.com");
        UserName userName = new UserName("Test User");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        testUser = new User(userId, email, userName, password);
    }

    @Test
    @DisplayName("should_MapUserToResponse_When_ValidUserProvided")
    void should_MapUserToResponse_When_ValidUserProvided() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response);
        assertEquals(testUser.getId().getValue().toString(), response.id());
        assertEquals(testUser.getEmail().getValue(), response.email());
        assertEquals(testUser.getUserName().getValue(), response.userName());
        assertEquals(testUser.isActive(), response.active());
        assertEquals(testUser.isEmailVerified(), response.emailVerified());
        assertEquals(testUser.getCreatedAt(), response.createdAt());
    }

    @Test
    @DisplayName("should_IncludeUserName_When_Mapping")
    void should_IncludeUserName_When_Mapping() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response.userName());
        assertEquals("Test User", response.userName());
        assertEquals(testUser.getUserName().getValue(), response.userName());
    }

    @Test
    @DisplayName("should_MapActiveStatus_When_UserIsActive")
    void should_MapActiveStatus_When_UserIsActive() {
        UserResponse response = userMapper.toResponse(testUser);

        assertTrue(response.active());
    }

    @Test
    @DisplayName("should_MapActiveStatus_When_UserIsInactive")
    void should_MapActiveStatus_When_UserIsInactive() {
        testUser.deactivate();
        
        UserResponse response = userMapper.toResponse(testUser);

        assertFalse(response.active());
    }

    @Test
    @DisplayName("should_MapEmailVerified_When_EmailIsVerified")
    void should_MapEmailVerified_When_EmailIsVerified() {
        testUser.verifyEmail();
        
        UserResponse response = userMapper.toResponse(testUser);

        assertTrue(response.emailVerified());
    }

    @Test
    @DisplayName("should_MapEmailVerified_When_EmailIsNotVerified")
    void should_MapEmailVerified_When_EmailIsNotVerified() {
        UserResponse response = userMapper.toResponse(testUser);

        assertFalse(response.emailVerified());
    }

    @Test
    @DisplayName("should_MapUserId_When_Mapping")
    void should_MapUserId_When_Mapping() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response.id());
        assertEquals(testUser.getId().getValue().toString(), response.id());
    }

    @Test
    @DisplayName("should_MapEmail_When_Mapping")
    void should_MapEmail_When_Mapping() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response.email());
        assertEquals("test@example.com", response.email());
        assertEquals(testUser.getEmail().getValue(), response.email());
    }

    @Test
    @DisplayName("should_MapCreatedAt_When_Mapping")
    void should_MapCreatedAt_When_Mapping() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response.createdAt());
        assertEquals(testUser.getCreatedAt(), response.createdAt());
    }

    @Test
    @DisplayName("should_MapAllFields_When_ConvertingToResponse")
    void should_MapAllFields_When_ConvertingToResponse() {
        UserResponse response = userMapper.toResponse(testUser);

        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(response.email());
        assertNotNull(response.userName());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertFalse(response.emailVerified());
        assertTrue(response.active());
    }
}