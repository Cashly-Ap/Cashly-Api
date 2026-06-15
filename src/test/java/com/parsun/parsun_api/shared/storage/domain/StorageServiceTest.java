package com.parsun.parsun_api.shared.storage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StorageService Exception Hierarchy Tests")
class StorageServiceTest {

    @Test
    @DisplayName("should create storage exception with message")
    void should_create_storage_exception_with_message() {
        StorageException exception = new StorageException("Storage error occurred");

        assertEquals("Storage error occurred", exception.getMessage());
    }

    @Test
    @DisplayName("should create storage exception with message and cause")
    void should_create_storage_exception_with_message_and_cause() {
        Throwable cause = new RuntimeException("Underlying error");
        StorageException exception = new StorageException("Storage error occurred", cause);

        assertEquals("Storage error occurred", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("should be runtime exception")
    void should_be_runtime_exception() {
        StorageException exception = new StorageException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should create file not found exception with correct message")
    void should_create_file_not_found_exception_with_correct_message() {
        FileNotFoundException exception = new FileNotFoundException("/path/to/file.txt");

        assertEquals("File not found: /path/to/file.txt", exception.getMessage());
    }

    @Test
    @DisplayName("should be instance of storage exception")
    void should_be_instance_of_storage_exception() {
        FileNotFoundException exception = new FileNotFoundException("test.txt");

        assertInstanceOf(StorageException.class, exception);
    }

    @Test
    @DisplayName("should be instance of runtime exception")
    void should_be_instance_of_runtime_exception() {
        FileNotFoundException exception = new FileNotFoundException("test.txt");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should preserve file path in message")
    void should_preserve_file_path_in_message() {
        String filePath = "/data/uploads/invoice.pdf";
        FileNotFoundException exception = new FileNotFoundException(filePath);

        assertTrue(exception.getMessage().contains(filePath));
    }
}
