package com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebitNoteId Value Object Unit Tests")
class DebitNoteIdUnitTest {

    @Test
    void should_CreateDebitNoteId_When_ValidUUIDProvided() {
        UUID uuid = UUID.randomUUID();
        DebitNoteId id = DebitNoteId.of(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void should_GenerateDebitNoteId_When_UsingGenerate() {
        DebitNoteId id = DebitNoteId.generate();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void should_GenerateUniqueIds_When_CalledMultipleTimes() {
        DebitNoteId first = DebitNoteId.generate();
        DebitNoteId second = DebitNoteId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new DebitNoteId(null));
    }

    @Test
    void should_ThrowException_When_OfWithNull() {
        assertThrows(NullPointerException.class, () -> DebitNoteId.of(null));
    }

    @Test
    void should_BeEqual_When_SameUUIDProvided() {
        UUID uuid = UUID.randomUUID();
        DebitNoteId first = DebitNoteId.of(uuid);
        DebitNoteId second = DebitNoteId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUUIDsProvided() {
        DebitNoteId first = DebitNoteId.of(UUID.randomUUID());
        DebitNoteId second = DebitNoteId.of(UUID.randomUUID());

        assertNotEquals(first, second);
    }

    @Test
    void should_ReturnStringRepresentation_When_ToStringCalled() {
        UUID uuid = UUID.randomUUID();
        DebitNoteId id = DebitNoteId.of(uuid);

        assertEquals("DebitNoteId[value=" + uuid + "]", id.toString());
    }
}
