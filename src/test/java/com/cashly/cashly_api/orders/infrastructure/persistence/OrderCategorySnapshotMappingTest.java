package com.cashly.cashly_api.orders.infrastructure.persistence;

import com.cashly.cashly_api.orders.domain.OrderLine;
import com.cashly.cashly_api.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.orders.infrastructure.persistence.entities.OrderLineEntity;
import com.cashly.cashly_api.orders.infrastructure.persistence.mappers.OrderJpaMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderCategorySnapshotMappingTest {
    @Test
    void mapsHistoricalCategoryThroughPersistence() {
        UUID categoryId = UUID.randomUUID();
        OrderLine line = OrderLine.create(UUID.randomUUID(), "Product", "SKU", categoryId,
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        var order = com.cashly.cashly_api.orders.domain.Order.create(
                com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber.of(2026, 1), UUID.randomUUID(),
                "Customer", "RIF", UUID.randomUUID(), "Actor", null, List.of(line));

        OrderEntity persisted = OrderJpaMapper.toEntity(order);
        OrderLineEntity lineEntity = persisted.getLines().getFirst();
        var restored = OrderJpaMapper.toDomain(persisted).getLines().getFirst();

        assertEquals(categoryId, lineEntity.getCategoryIdSnapshot());
        assertEquals(categoryId, restored.getCategoryId());
    }

    @Test
    void keepsLegacyCategorySnapshotAbsent() {
        OrderLine line = OrderLine.create(UUID.randomUUID(), "Product", "SKU",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        var order = com.cashly.cashly_api.orders.domain.Order.create(
                com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber.of(2026, 1), UUID.randomUUID(),
                "Customer", "RIF", UUID.randomUUID(), "Actor", null, List.of(line));

        var persisted = OrderJpaMapper.toEntity(order);

        assertNull(persisted.getLines().getFirst().getCategoryIdSnapshot());
    }
}
