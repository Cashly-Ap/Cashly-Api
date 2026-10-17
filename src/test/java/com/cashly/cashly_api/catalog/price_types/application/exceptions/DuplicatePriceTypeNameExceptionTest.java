package com.cashly.cashly_api.catalog.price_types.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicatePriceTypeNameException Unit Tests")
class DuplicatePriceTypeNameExceptionTest {

    @Test
    void should_CreateException_WithNameInMessage() {
        String name = "Mayorista";

        DuplicatePriceTypeNameException exception = new DuplicatePriceTypeNameException(name);

        assertTrue(exception.getMessage().contains(name));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void should_BeRuntimeException() {
        DuplicatePriceTypeNameException exception = new DuplicatePriceTypeNameException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        DuplicatePriceTypeNameException exception = new DuplicatePriceTypeNameException("Premium");

        assertNotNull(exception);
    }
}
