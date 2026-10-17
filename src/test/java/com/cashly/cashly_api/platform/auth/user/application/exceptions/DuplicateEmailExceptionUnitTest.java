package com.cashly.cashly_api.platform.auth.user.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicateEmailException Unit Tests")
class DuplicateEmailExceptionUnitTest {

    @Test
    @DisplayName("should_CreateException_When_MessageProvided")
    void should_CreateException_When_MessageProvided() {
        String message = "Email already exists";
        
        DuplicateEmailException exception = new DuplicateEmailException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("should_BeRuntimeException_When_Created")
    void should_BeRuntimeException_When_Created() {
        DuplicateEmailException exception = new DuplicateEmailException("test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should_BeThrowable_When_Created")
    void should_BeThrowable_When_Created() {
        String message = "User with email test@example.com already exists";
        
        assertThrows(DuplicateEmailException.class, () -> {
            throw new DuplicateEmailException(message);
        });
    }

    @Test
    @DisplayName("should_ContainMessage_When_Thrown")
    void should_ContainMessage_When_Thrown() {
        String message = "Email test@example.com is already registered";
        
        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> { throw new DuplicateEmailException(message); }
        );
        
        assertEquals(message, exception.getMessage());
    }
}
