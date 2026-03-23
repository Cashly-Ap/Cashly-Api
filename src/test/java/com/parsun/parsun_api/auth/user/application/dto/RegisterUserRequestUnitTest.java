package com.parsun.parsun_api.auth.user.application.dto;

import com.parsun.parsun_api.auth.user.application.dto.request.RegisterUserRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterUserRequest Unit Tests")
class RegisterUserRequestUnitTest {

    @Test
    @DisplayName("should_CreateRegisterUserRequest_When_ValidDataProvided")
    void should_CreateRegisterUserRequest_When_ValidDataProvided() {
        RegisterUserRequest request = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "SecurePassword123!"
        );

        assertNotNull(request);
        assertEquals("test@example.com", request.email());
        assertEquals("Test User", request.userName());
        assertEquals("SecurePassword123!", request.password());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsNull")
    void should_ThrowException_When_EmailIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest(null, "Test User", "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsEmpty")
    void should_ThrowException_When_EmailIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("", "Test User", "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsBlank")
    void should_ThrowException_When_EmailIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("   ", "Test User", "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNameIsNull")
    void should_ThrowException_When_UserNameIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("test@example.com", null, "password")
        );

        assertEquals("User name is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNameIsEmpty")
    void should_ThrowException_When_UserNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("test@example.com", "", "password")
        );

        assertEquals("User name is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNameIsBlank")
    void should_ThrowException_When_UserNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("test@example.com", "   ", "password")
        );

        assertEquals("User name is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_PasswordIsNull")
    void should_ThrowException_When_PasswordIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("test@example.com", "Test User", null)
        );

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_PasswordIsEmpty")
    void should_ThrowException_When_PasswordIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterUserRequest("test@example.com", "Test User", "")
        );

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_BeEqual_When_AllFieldsMatch")
    void should_BeEqual_When_AllFieldsMatch() {
        RegisterUserRequest request1 = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "password"
        );
        RegisterUserRequest request2 = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "password"
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("should_BeImmutable_When_Created")
    void should_BeImmutable_When_Created() {
        RegisterUserRequest request = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "password"
        );

        assertEquals("test@example.com", request.email());
        assertEquals("Test User", request.userName());
        assertEquals("password", request.password());
    }

    @Test
    @DisplayName("should_GenerateToString_When_Called")
    void should_GenerateToString_When_Called() {
        RegisterUserRequest request = new RegisterUserRequest(
            "test@example.com",
            "Test User",
            "password"
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Test User"));
    }
}
