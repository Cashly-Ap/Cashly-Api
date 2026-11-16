package com.cashly.cashly_api.catalog.products.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductComponent Entity Unit Tests")
class ProductComponentUnitTest {

    @Test
    void should_Create_When_ValidArguments() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        ProductComponent component = new ProductComponent(id, parentId,
                componentId, 3);

        assertEquals(id, component.getId());
        assertEquals(parentId, component.getParentProductId());
        assertEquals(componentId, component.getComponentProductId());
        assertEquals(3, component.getQuantity());
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> new ProductComponent(id, parentId, componentId, 0));
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> new ProductComponent(id, parentId, componentId, -1));
    }

    @Test
    void should_ThrowException_When_ComponentProductIdIsNull() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ProductComponent(id, parentId, null, 1));

        assertEquals("Component product ID cannot be null",
                exception.getMessage());
    }

    @Test
    void should_AllowQuantityOfOne() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        ProductComponent component = new ProductComponent(id, parentId,
                componentId, 1);

        assertEquals(1, component.getQuantity());
    }
}
