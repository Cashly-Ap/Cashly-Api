package com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceId Value Object Unit Tests")
class InvoiceIdUnitTest {

    @Test
    void should_CreateInvoiceId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        InvoiceId invoiceId = InvoiceId.of(uuid);

        assertEquals(uuid, invoiceId.value());
    }

    @Test
    void should_GenerateUniqueId_When_GenerateCalled() {
        InvoiceId first = InvoiceId.generate();
        InvoiceId second = InvoiceId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_GenerateNonNullId() {
        InvoiceId invoiceId = InvoiceId.generate();

        assertNotNull(invoiceId);
        assertNotNull(invoiceId.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new InvoiceId(null));
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        InvoiceId first = InvoiceId.of(uuid);
        InvoiceId second = InvoiceId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsProvided() {
        InvoiceId first = InvoiceId.generate();
        InvoiceId second = InvoiceId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_HaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();
        InvoiceId invoiceId = InvoiceId.of(uuid);

        assertEquals(uuid.hashCode(), invoiceId.hashCode());
    }
}
