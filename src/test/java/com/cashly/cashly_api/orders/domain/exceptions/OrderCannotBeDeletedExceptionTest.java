package com.cashly.cashly_api.orders.domain.exceptions;

import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderCannotBeDeletedException Unit Tests")
class OrderCannotBeDeletedExceptionTest {

    @Test
    void should_CreateException_WithIdAndStatus() {
        UUID id = UUID.randomUUID();

        OrderCannotBeDeletedException exception = new OrderCannotBeDeletedException(id, OrderStatus.CONFIRMED);

        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("CONFIRMED"));
    }

    @Test
    void should_BeRuntimeException() {
        OrderCannotBeDeletedException exception =
                new OrderCannotBeDeletedException(UUID.randomUUID(), OrderStatus.DRAFT);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_HandleDifferentStatuses() {
        UUID id = UUID.randomUUID();

        OrderCannotBeDeletedException draftEx = new OrderCannotBeDeletedException(id, OrderStatus.DRAFT);
        OrderCannotBeDeletedException confirmedEx = new OrderCannotBeDeletedException(id, OrderStatus.CONFIRMED);
        OrderCannotBeDeletedException invoicedEx = new OrderCannotBeDeletedException(id, OrderStatus.INVOICED);

        assertTrue(draftEx.getMessage().contains("DRAFT"));
        assertTrue(confirmedEx.getMessage().contains("CONFIRMED"));
        assertTrue(invoicedEx.getMessage().contains("INVOICED"));
    }
}
