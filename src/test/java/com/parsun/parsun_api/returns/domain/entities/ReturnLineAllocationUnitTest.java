package com.parsun.parsun_api.returns.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReturnLineAllocation Value Object Unit Tests")
class ReturnLineAllocationUnitTest {

    @Test
    void should_CreateAllocation_When_ValidData() {
        UUID warehouseId = UUID.randomUUID();
        BigDecimal allocated = BigDecimal.TEN;

        ReturnLineAllocation allocation = ReturnLineAllocation.create(warehouseId, allocated);

        assertNotNull(allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(allocated, allocation.getAllocated());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        BigDecimal allocated = BigDecimal.TEN;

        ReturnLineAllocation allocation = ReturnLineAllocation.reconstitute(id, warehouseId, allocated);

        assertEquals(id, allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(allocated, allocation.getAllocated());
    }

    @Test
    void should_GenerateUniqueId_When_CreateCalled() {
        ReturnLineAllocation allocation1 = ReturnLineAllocation.create(UUID.randomUUID(), BigDecimal.ONE);
        ReturnLineAllocation allocation2 = ReturnLineAllocation.create(UUID.randomUUID(), BigDecimal.ONE);

        assertNotEquals(allocation1.getId(), allocation2.getId());
    }
}
