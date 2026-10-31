package com.cashly.cashly_api.platform.auth.user.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserNotFoundException Unit Tests")
class UserNotFoundExceptionUnitTest {

    @Test
    @DisplayName("should_CreateException_When_MessageProvided")
    void should_CreateException_When_MessageProvided() {
        String message = "User not found";

        UserNotFoundException exception = new UserNotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("should_BeRuntimeException_When_Created")
    void should_BeRuntimeException_When_Created() {
        UserNotFoundException exception = new UserNotFoundException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should_BeThrowable_When_Created")
    void should_BeThrowable_When_Created() {
        String message = "User does not exist";

        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(message);
        });
    }

    @Test
    @DisplayName("should_ContainMessage_When_Thrown")
    void should_ContainMessage_When_Thrown() {
        String message = "User with ID user-id-123 was not found";

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class, () -> {
                    throw new UserNotFoundException(message);
                });

        assertEquals(message, exception.getMessage());
    }
}
