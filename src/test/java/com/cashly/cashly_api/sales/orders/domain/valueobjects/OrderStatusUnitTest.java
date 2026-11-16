package com.cashly.cashly_api.sales.orders.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderStatus Value Object Unit Tests")
class OrderStatusUnitTest {

    @Test
    void should_HaveAllExpectedEnumValues() {
        OrderStatus[] values = OrderStatus.values();

        assertEquals(4, values.length);
        assertNotNull(OrderStatus.valueOf("DRAFT"));
        assertNotNull(OrderStatus.valueOf("CONFIRMED"));
        assertNotNull(OrderStatus.valueOf("INVOICED"));
        assertNotNull(OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ReturnDraft_When_ValueOfDraft() {
        assertEquals(OrderStatus.DRAFT, OrderStatus.valueOf("DRAFT"));
    }

    @Test
    void should_ReturnConfirmed_When_ValueOfConfirmed() {
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
    }

    @Test
    void should_ReturnInvoiced_When_ValueOfInvoiced() {
        assertEquals(OrderStatus.INVOICED, OrderStatus.valueOf("INVOICED"));
    }

    @Test
    void should_ReturnCancelled_When_ValueOfCancelled() {
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void should_ThrowException_When_InvalidStatusName() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.valueOf("INVALID"));
    }
}
