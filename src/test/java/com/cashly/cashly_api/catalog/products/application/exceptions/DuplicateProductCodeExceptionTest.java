package com.cashly.cashly_api.catalog.products.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicateProductCodeException Unit Tests")
class DuplicateProductCodeExceptionTest {

    @Test
    void should_CreateException_WithCodeInMessage() {
        String code = "PROD-001";

        DuplicateProductCodeException exception = new DuplicateProductCodeException(code);

        assertTrue(exception.getMessage().contains(code));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void should_BeRuntimeException() {
        DuplicateProductCodeException exception = new DuplicateProductCodeException("TEST");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_NotBeNull() {
        DuplicateProductCodeException exception = new DuplicateProductCodeException("PROD-002");

        assertNotNull(exception);
    }
}
