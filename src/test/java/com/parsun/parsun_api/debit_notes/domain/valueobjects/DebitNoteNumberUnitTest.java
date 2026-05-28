package com.parsun.parsun_api.debit_notes.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebitNoteNumber Value Object Unit Tests")
class DebitNoteNumberUnitTest {

    @Test
    void should_CreateDebitNoteNumber_When_ValidYearAndSequenceProvided() {
        DebitNoteNumber number = DebitNoteNumber.of(2026, 1);

        assertEquals("DN-2026-0001", number.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan4Digits() {
        DebitNoteNumber number = DebitNoteNumber.of(2026, 42);

        assertEquals("DN-2026-0042", number.value());
    }

    @Test
    void should_NotPad_When_SequenceHas4Digits() {
        DebitNoteNumber number = DebitNoteNumber.of(2026, 9999);

        assertEquals("DN-2026-9999", number.value());
    }

    @Test
    void should_CreateDebitNoteNumber_When_DirectValueProvided() {
        DebitNoteNumber number = new DebitNoteNumber("DN-2026-0001");

        assertEquals("DN-2026-0001", number.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new DebitNoteNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class, () -> new DebitNoteNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class, () -> DebitNoteNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> DebitNoteNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        DebitNoteNumber first = new DebitNoteNumber("DN-2026-0001");
        DebitNoteNumber second = new DebitNoteNumber("DN-2026-0001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        DebitNoteNumber first = DebitNoteNumber.of(2026, 1);
        DebitNoteNumber second = DebitNoteNumber.of(2026, 2);

        assertNotEquals(first, second);
    }
}
