package com.parsun.parsun_api.orders.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderNotPendingException Unit Tests")
class OrderNotPendingExceptionTest {

    @Test
    void should_CreateException_WithActionInMessage() {
        OrderNotPendingException exception = new OrderNotPendingException("cancel");

        assertTrue(exception.getMessage().contains("cancel"));
        assertTrue(exception.getMessage().contains("PENDING"));
    }

    @Test
    void should_BeRuntimeException() {
        OrderNotPendingException exception = new OrderNotPendingException("confirm");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void should_HandleDifferentActions() {
        OrderNotPendingException cancelEx = new OrderNotPendingException("cancel");
        OrderNotPendingException confirmEx = new OrderNotPendingException("confirm");
        OrderNotPendingException deleteEx = new OrderNotPendingException("delete");

        assertTrue(cancelEx.getMessage().contains("cancel"));
        assertTrue(confirmEx.getMessage().contains("confirm"));
        assertTrue(deleteEx.getMessage().contains("delete"));
    }
}
