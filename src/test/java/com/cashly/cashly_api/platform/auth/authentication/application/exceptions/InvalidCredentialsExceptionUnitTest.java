package com.cashly.cashly_api.platform.auth.authentication.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidCredentialsException Unit Tests")
class InvalidCredentialsExceptionUnitTest {

    @Test
    @DisplayName("should_CreateException_When_MessageProvided")
    void should_CreateException_When_MessageProvided() {
        String message = "Invalid email or password";

        InvalidCredentialsException exception = new InvalidCredentialsException(
                message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("should_BeRuntimeException_When_Created")
    void should_BeRuntimeException_When_Created() {
        InvalidCredentialsException exception = new InvalidCredentialsException(
                "test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should_BeThrowable_When_Created")
    void should_BeThrowable_When_Created() {
        String message = "Invalid credentials provided";

        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException(message);
        });
    }

    @Test
    @DisplayName("should_ContainMessage_When_Thrown")
    void should_ContainMessage_When_Thrown() {
        String message = "Authentication failed: invalid credentials";

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class, () -> {
                    throw new InvalidCredentialsException(message);
                });

        assertEquals(message, exception.getMessage());
    }
}
