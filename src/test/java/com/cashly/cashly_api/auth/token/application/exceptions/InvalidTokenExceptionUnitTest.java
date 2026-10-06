package com.cashly.cashly_api.auth.token.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidTokenException Unit Tests")
class InvalidTokenExceptionUnitTest {

    @Test
    @DisplayName("should_CreateException_When_MessageProvided")
    void should_CreateException_When_MessageProvided() {
        String message = "Invalid token";
        
        InvalidTokenException exception = new InvalidTokenException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("should_BeRuntimeException_When_Created")
    void should_BeRuntimeException_When_Created() {
        InvalidTokenException exception = new InvalidTokenException("test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should_BeThrowable_When_Created")
    void should_BeThrowable_When_Created() {
        String message = "The provided token is invalid";
        
        assertThrows(InvalidTokenException.class, () -> {
            throw new InvalidTokenException(message);
        });
    }

    @Test
    @DisplayName("should_ContainMessage_When_Thrown")
    void should_ContainMessage_When_Thrown() {
        String message = "Refresh token is invalid or has been revoked";
        
        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> { throw new InvalidTokenException(message); }
        );
        
        assertEquals(message, exception.getMessage());
    }
}
