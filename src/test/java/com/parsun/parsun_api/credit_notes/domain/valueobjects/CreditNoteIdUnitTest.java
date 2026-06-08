package com.parsun.parsun_api.credit_notes.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditNoteId Value Object Unit Tests")
class CreditNoteIdUnitTest {

    @Test
    void should_CreateCreditNoteId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        CreditNoteId creditNoteId = CreditNoteId.of(uuid);

        assertEquals(uuid, creditNoteId.value());
    }

    @Test
    void should_GenerateUniqueId_When_GenerateCalled() {
        CreditNoteId first = CreditNoteId.generate();
        CreditNoteId second = CreditNoteId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_GenerateNonNullId() {
        CreditNoteId creditNoteId = CreditNoteId.generate();

        assertNotNull(creditNoteId);
        assertNotNull(creditNoteId.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new CreditNoteId(null));
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        CreditNoteId first = CreditNoteId.of(uuid);
        CreditNoteId second = CreditNoteId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsProvided() {
        CreditNoteId first = CreditNoteId.generate();
        CreditNoteId second = CreditNoteId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_HaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();
        CreditNoteId creditNoteId = CreditNoteId.of(uuid);

        assertEquals(uuid.hashCode(), creditNoteId.hashCode());
    }

    @Test
    void should_ReturnUuidString_When_ToStringCalled() {
        UUID uuid = UUID.randomUUID();
        CreditNoteId creditNoteId = CreditNoteId.of(uuid);

        assertEquals("CreditNoteId[value=" + uuid + "]", creditNoteId.toString());
    }
}
