package com.cashly.cashly_api.invoices.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceNumber Value Object Unit Tests")
class InvoiceNumberUnitTest {

    @Test
    void should_CreateInvoiceNumber_When_ValidYearAndSequenceProvided() {
        InvoiceNumber invoiceNumber = InvoiceNumber.of(2026, 1);

        assertEquals("INV-2026-00001", invoiceNumber.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan5Digits() {
        InvoiceNumber invoiceNumber = InvoiceNumber.of(2026, 42);

        assertEquals("INV-2026-00042", invoiceNumber.value());
    }

    @Test
    void should_NotPad_When_SequenceHas5Digits() {
        InvoiceNumber invoiceNumber = InvoiceNumber.of(2026, 99999);

        assertEquals("INV-2026-99999", invoiceNumber.value());
    }

    @Test
    void should_CreateInvoiceNumber_When_DirectValueProvided() {
        InvoiceNumber invoiceNumber = new InvoiceNumber("INV-2026-00001");

        assertEquals("INV-2026-00001", invoiceNumber.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new InvoiceNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class, () -> new InvoiceNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        InvoiceNumber first = new InvoiceNumber("INV-2026-00001");
        InvoiceNumber second = new InvoiceNumber("INV-2026-00001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        InvoiceNumber first = InvoiceNumber.of(2026, 1);
        InvoiceNumber second = InvoiceNumber.of(2026, 2);

        assertNotEquals(first, second);
    }
}
