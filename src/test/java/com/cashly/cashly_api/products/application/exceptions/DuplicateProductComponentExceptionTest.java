package com.cashly.cashly_api.products.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicateProductComponentException Unit Tests")
class DuplicateProductComponentExceptionTest {

    @Test
    void should_CreateException_WithComponentIdInMessage() {
        UUID componentId = UUID.randomUUID();

        DuplicateProductComponentException exception =
                new DuplicateProductComponentException(componentId);

        assertTrue(exception.getMessage().contains(componentId.toString()));
    }

    @Test
    void should_BeRuntimeException() {
        DuplicateProductComponentException exception =
                new DuplicateProductComponentException(UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        DuplicateProductComponentException exception =
                new DuplicateProductComponentException(UUID.randomUUID());

        assertNotNull(exception);
    }
}
