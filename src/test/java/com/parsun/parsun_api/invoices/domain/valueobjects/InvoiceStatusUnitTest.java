package com.parsun.parsun_api.invoices.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceStatus Value Object Unit Tests")
class InvoiceStatusUnitTest {

    @Test
    void should_HaveAllExpectedEnumValues() {
        InvoiceStatus[] values = InvoiceStatus.values();

        assertEquals(6, values.length);
        assertNotNull(InvoiceStatus.valueOf("DRAFT"));
        assertNotNull(InvoiceStatus.valueOf("PAID"));
        assertNotNull(InvoiceStatus.valueOf("PENDING_PAYMENT"));
        assertNotNull(InvoiceStatus.valueOf("PARTIALLY_PAID"));
        assertNotNull(InvoiceStatus.valueOf("FULLY_RETURNED"));
        assertNotNull(InvoiceStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ReturnDraft_When_ValueOfDraft() {
        assertEquals(InvoiceStatus.DRAFT, InvoiceStatus.valueOf("DRAFT"));
    }

    @Test
    void should_ReturnPaid_When_ValueOfPaid() {
        assertEquals(InvoiceStatus.PAID, InvoiceStatus.valueOf("PAID"));
    }

    @Test
    void should_ReturnPendingPayment_When_ValueOfPendingPayment() {
        assertEquals(InvoiceStatus.PENDING_PAYMENT, InvoiceStatus.valueOf("PENDING_PAYMENT"));
    }

    @Test
    void should_ReturnPartiallyPaid_When_ValueOfPartiallyPaid() {
        assertEquals(InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.valueOf("PARTIALLY_PAID"));
    }

    @Test
    void should_ReturnCancelled_When_ValueOfCancelled() {
        assertEquals(InvoiceStatus.CANCELLED, InvoiceStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ReturnFullyReturned_When_ValueOfFullyReturned() {
        assertEquals(InvoiceStatus.FULLY_RETURNED, InvoiceStatus.valueOf("FULLY_RETURNED"));
    }

    @Test
    void should_ThrowException_When_InvalidStatusName() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceStatus.valueOf("INVALID"));
    }
}
