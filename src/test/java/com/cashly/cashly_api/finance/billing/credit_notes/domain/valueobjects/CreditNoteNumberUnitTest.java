package com.cashly.cashly_api.finance.billing.credit_notes.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditNoteNumber Value Object Unit Tests")
class CreditNoteNumberUnitTest {

    @Test
    void should_CreateCreditNoteNumber_When_ValidYearAndSequenceProvided() {
        CreditNoteNumber creditNoteNumber = CreditNoteNumber.of(2026, 1);

        assertEquals("CN-2026-0001", creditNoteNumber.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan4Digits() {
        CreditNoteNumber creditNoteNumber = CreditNoteNumber.of(2026, 42);

        assertEquals("CN-2026-0042", creditNoteNumber.value());
    }

    @Test
    void should_NotPad_When_SequenceHas4Digits() {
        CreditNoteNumber creditNoteNumber = CreditNoteNumber.of(2026, 9999);

        assertEquals("CN-2026-9999", creditNoteNumber.value());
    }

    @Test
    void should_CreateCreditNoteNumber_When_DirectValueProvided() {
        CreditNoteNumber creditNoteNumber = new CreditNoteNumber(
                "CN-2026-0001");

        assertEquals("CN-2026-0001", creditNoteNumber.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class,
                () -> new CreditNoteNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreditNoteNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> CreditNoteNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> CreditNoteNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        CreditNoteNumber first = new CreditNoteNumber("CN-2026-0001");
        CreditNoteNumber second = new CreditNoteNumber("CN-2026-0001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        CreditNoteNumber first = CreditNoteNumber.of(2026, 1);
        CreditNoteNumber second = CreditNoteNumber.of(2026, 2);

        assertNotEquals(first, second);
    }
}
