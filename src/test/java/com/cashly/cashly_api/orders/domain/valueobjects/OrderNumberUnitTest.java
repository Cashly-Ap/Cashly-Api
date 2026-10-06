package com.cashly.cashly_api.orders.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderNumber Value Object Unit Tests")
class OrderNumberUnitTest {

    @Test
    void should_CreateOrderNumber_When_ValidYearAndSequenceProvided() {
        OrderNumber orderNumber = OrderNumber.of(2026, 1);

        assertEquals("ORD-2026-00001", orderNumber.value());
    }

    @Test
    void should_PadSequence_When_SequenceHasLessThan5Digits() {
        OrderNumber orderNumber = OrderNumber.of(2026, 42);

        assertEquals("ORD-2026-00042", orderNumber.value());
    }

    @Test
    void should_NotPad_When_SequenceHas5Digits() {
        OrderNumber orderNumber = OrderNumber.of(2026, 99999);

        assertEquals("ORD-2026-99999", orderNumber.value());
    }

    @Test
    void should_CreateOrderNumber_When_DirectValueProvided() {
        OrderNumber orderNumber = new OrderNumber("ORD-2026-00001");

        assertEquals("ORD-2026-00001", orderNumber.value());
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new OrderNumber(null));
    }

    @Test
    void should_ThrowException_When_BlankValueProvided() {
        assertThrows(IllegalArgumentException.class, () -> new OrderNumber("   "));
    }

    @Test
    void should_ThrowException_When_SequenceIsZero() {
        assertThrows(IllegalArgumentException.class, () -> OrderNumber.of(2026, 0));
    }

    @Test
    void should_ThrowException_When_SequenceIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> OrderNumber.of(2026, -1));
    }

    @Test
    void should_BeEqual_When_SameValueProvided() {
        OrderNumber first = new OrderNumber("ORD-2026-00001");
        OrderNumber second = new OrderNumber("ORD-2026-00001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValuesProvided() {
        OrderNumber first = OrderNumber.of(2026, 1);
        OrderNumber second = OrderNumber.of(2026, 2);

        assertNotEquals(first, second);
    }
}
