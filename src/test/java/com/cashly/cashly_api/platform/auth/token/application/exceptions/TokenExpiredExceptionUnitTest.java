package com.cashly.cashly_api.platform.auth.token.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenExpiredException Unit Tests")
class TokenExpiredExceptionUnitTest {

    @Test
    @DisplayName("should_CreateException_When_MessageProvided")
    void should_CreateException_When_MessageProvided() {
        String message = "Token has expired";
        
        TokenExpiredException exception = new TokenExpiredException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("should_BeRuntimeException_When_Created")
    void should_BeRuntimeException_When_Created() {
        TokenExpiredException exception = new TokenExpiredException("test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should_BeThrowable_When_Created")
    void should_BeThrowable_When_Created() {
        String message = "The token has expired";
        
        assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });
    }

    @Test
    @DisplayName("should_ContainMessage_When_Thrown")
    void should_ContainMessage_When_Thrown() {
        String message = "Refresh token has expired and must be renewed";
        
        TokenExpiredException exception = assertThrows(
            TokenExpiredException.class,
            () -> { throw new TokenExpiredException(message); }
        );
        
        assertEquals(message, exception.getMessage());
    }
}
