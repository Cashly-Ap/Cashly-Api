package com.cashly.cashly_api.catalog.products.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompositeProductCannotBeComponentException Unit Tests")
class CompositeProductCannotBeComponentExceptionTest {

    @Test
    void should_CreateException_WithIdInMessage() {
        UUID id = UUID.randomUUID();

        CompositeProductCannotBeComponentException exception =
                new CompositeProductCannotBeComponentException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("cannot be used as a component"));
    }

    @Test
    void should_BeRuntimeException() {
        CompositeProductCannotBeComponentException exception =
                new CompositeProductCannotBeComponentException(UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_BeThrownAndCaught() {
        assertThrows(CompositeProductCannotBeComponentException.class,
                () -> { throw new CompositeProductCannotBeComponentException(UUID.randomUUID()); });
    }
}
