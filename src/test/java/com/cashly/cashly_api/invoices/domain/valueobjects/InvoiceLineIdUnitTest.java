package com.cashly.cashly_api.invoices.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceLineId Value Object Unit Tests")
class InvoiceLineIdUnitTest {

    @Test
    void should_CreateInvoiceLineId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        InvoiceLineId lineId = InvoiceLineId.of(uuid);

        assertEquals(uuid, lineId.value());
    }

    @Test
    void should_GenerateUniqueId_When_GenerateCalled() {
        InvoiceLineId first = InvoiceLineId.generate();
        InvoiceLineId second = InvoiceLineId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_GenerateNonNullId() {
        InvoiceLineId lineId = InvoiceLineId.generate();

        assertNotNull(lineId);
        assertNotNull(lineId.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new InvoiceLineId(null));
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        InvoiceLineId first = InvoiceLineId.of(uuid);
        InvoiceLineId second = InvoiceLineId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsProvided() {
        InvoiceLineId first = InvoiceLineId.generate();
        InvoiceLineId second = InvoiceLineId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_HaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();
        InvoiceLineId lineId = InvoiceLineId.of(uuid);

        assertEquals(uuid.hashCode(), lineId.hashCode());
    }
}
