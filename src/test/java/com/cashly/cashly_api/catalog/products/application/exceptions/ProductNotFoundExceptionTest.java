package com.cashly.cashly_api.catalog.products.application.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductNotFoundException Unit Tests")
class ProductNotFoundExceptionTest {

    @Test
    @DisplayName("should create exception with correct message")
    void should_create_exception_with_correct_message() {
        UUID id = UUID.randomUUID();

        ProductNotFoundException exception = new ProductNotFoundException(id);

        assertEquals("Product not found with id: " + id, exception.getMessage());
    }

    @Test
    @DisplayName("should be runtime exception")
    void should_be_runtime_exception() {
        ProductNotFoundException exception = new ProductNotFoundException(UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("should preserve id in message when null")
    void should_preserve_id_in_message_when_null() {
        ProductNotFoundException exception = new ProductNotFoundException(null);

        assertEquals("Product not found with id: null", exception.getMessage());
    }
}
