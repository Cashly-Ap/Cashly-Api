package com.cashly.cashly_api.finance.billing.invoices.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceNotFoundException Unit Tests")
class InvoiceNotFoundExceptionTest {

    @Test
    void should_CreateException_WithCorrectMessage() {
        UUID id = UUID.randomUUID();

        InvoiceNotFoundException exception = new InvoiceNotFoundException(id);

        assertEquals("Invoice not found with id: " + id,
                exception.getMessage());
    }

    @Test
    void should_BeRuntimeException() {
        InvoiceNotFoundException exception = new InvoiceNotFoundException(
                UUID.randomUUID());

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_PreserveIdInMessage_When_NullId() {
        InvoiceNotFoundException exception = new InvoiceNotFoundException(null);

        assertEquals("Invoice not found with id: null", exception.getMessage());
    }
}
