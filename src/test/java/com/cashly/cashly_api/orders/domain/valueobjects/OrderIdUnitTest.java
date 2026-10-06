package com.cashly.cashly_api.orders.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderId Value Object Unit Tests")
class OrderIdUnitTest {

    @Test
    void should_CreateOrderId_When_ValidUuidProvided() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId = OrderId.of(uuid);

        assertEquals(uuid, orderId.value());
    }

    @Test
    void should_CreateOrderId_When_ValidStringProvided() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        OrderId orderId = OrderId.of(uuidString);

        assertEquals(UUID.fromString(uuidString), orderId.value());
    }

    @Test
    void should_GenerateUniqueId_When_GenerateCalled() {
        OrderId first = OrderId.generate();
        OrderId second = OrderId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void should_ThrowException_When_NullValueProvided() {
        assertThrows(NullPointerException.class, () -> new OrderId(null));
    }

    @Test
    void should_ThrowException_When_InvalidStringProvided() {
        assertThrows(IllegalArgumentException.class, () -> OrderId.of("not-a-uuid"));
    }

    @Test
    void should_BeEqual_When_SameUuidProvided() {
        UUID uuid = UUID.randomUUID();
        OrderId first = OrderId.of(uuid);
        OrderId second = OrderId.of(uuid);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUuidsProvided() {
        OrderId first = OrderId.generate();
        OrderId second = OrderId.generate();

        assertNotEquals(first, second);
    }
}
