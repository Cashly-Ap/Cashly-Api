package com.cashly.cashly_api.shared.storage.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StorageException Unit Tests")
class StorageExceptionTest {

    @Test
    void should_CreateException_WithMessage() {
        String message = "Failed to store file";

        StorageException exception = new StorageException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void should_CreateException_WithMessageAndCause() {
        String message = "Failed to store file";
        Throwable cause = new RuntimeException("Disk full");

        StorageException exception = new StorageException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void should_BeRuntimeException() {
        StorageException exception = new StorageException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_PreserveCauseMessage() {
        Throwable cause = new RuntimeException("Disk full");
        StorageException exception = new StorageException("Failed to store file", cause);

        assertEquals("Disk full", exception.getCause().getMessage());
    }

    @Test
    void should_AcceptEmptyMessage() {
        StorageException exception = new StorageException("");

        assertEquals("", exception.getMessage());
    }
}
