package com.cashly.cashly_api.auth.authentication.application.login.request.validation;

import com.cashly.cashly_api.auth.authentication.application.login.request.LoginRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginRequest Unit Tests")
class LoginRequestUnitTest {

    @Test
    @DisplayName("should_CreateLoginRequest_When_ValidDataProvided")
    void should_CreateLoginRequest_When_ValidDataProvided() {
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "SecurePassword123!"
        );

        assertNotNull(request);
        assertEquals("test@example.com", request.email());
        assertEquals("SecurePassword123!", request.password());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsNull")
    void should_ThrowException_When_EmailIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoginRequest(null, "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsEmpty")
    void should_ThrowException_When_EmailIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoginRequest("", "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_EmailIsBlank")
    void should_ThrowException_When_EmailIsBlank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoginRequest("   ", "password")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_PasswordIsNull")
    void should_ThrowException_When_PasswordIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoginRequest("test@example.com", null)
        );

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_PasswordIsEmpty")
    void should_ThrowException_When_PasswordIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoginRequest("test@example.com", "")
        );

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    @DisplayName("should_BeEqual_When_AllFieldsMatch")
    void should_BeEqual_When_AllFieldsMatch() {
        LoginRequest request1 = new LoginRequest("test@example.com", "password");
        LoginRequest request2 = new LoginRequest("test@example.com", "password");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("should_BeImmutable_When_Created")
    void should_BeImmutable_When_Created() {
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "password"
        );

        assertEquals("test@example.com", request.email());
        assertEquals("password", request.password());
    }

    @Test
    @DisplayName("should_GenerateToString_When_Called")
    void should_GenerateToString_When_Called() {
        LoginRequest request = new LoginRequest(
            "test@example.com",
            "password"
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
    }
}
