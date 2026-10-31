package com.cashly.cashly_api.procurement.purchases.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseNumber Value Object Unit Tests")
class PurchaseNumberUnitTest {

    @Test
    void should_CreatePurchaseNumber_When_ValidYearAndSequenceProvided() {
        PurchaseNumber purchaseNumber = PurchaseNumber.of(2026, 1);

        assertEquals("PUR-2026-00001", purchaseNumber.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan5Digits() {
        PurchaseNumber purchaseNumber = PurchaseNumber.of(2026, 42);

        assertEquals("PUR-2026-00042", purchaseNumber.value());
    }

    @Test
    void should_NotPad_When_SequenceHas5Digits() {
        PurchaseNumber purchaseNumber = PurchaseNumber.of(2026, 99999);

        assertEquals("PUR-2026-99999", purchaseNumber.value());
    }

    @Test
    void should_CreatePurchaseNumber_When_DirectValueProvided() {
        PurchaseNumber purchaseNumber = new PurchaseNumber("PUR-2026-00001");

        assertEquals("PUR-2026-00001", purchaseNumber.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class,
                () -> new PurchaseNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class,
                () -> new PurchaseNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> PurchaseNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> PurchaseNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        PurchaseNumber first = new PurchaseNumber("PUR-2026-00001");
        PurchaseNumber second = new PurchaseNumber("PUR-2026-00001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        PurchaseNumber first = PurchaseNumber.of(2026, 1);
        PurchaseNumber second = PurchaseNumber.of(2026, 2);

        assertNotEquals(first, second);
    }
}
