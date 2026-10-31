package com.cashly.cashly_api.finance.billing.invoices.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvoiceLineAllocation Domain Entity Unit Tests")
class InvoiceLineAllocationUnitTest {

    @Test
    void should_CreateAllocation_When_ValidDataProvided() {
        UUID warehouseId = UUID.randomUUID();
        BigDecimal allocated = BigDecimal.valueOf(10);

        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .create(warehouseId, allocated);

        assertNotNull(allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(allocated, allocation.getAllocated());
    }

    @Test
    void should_GenerateUniqueId_When_Created() {
        UUID warehouseId = UUID.randomUUID();

        InvoiceLineAllocation first = InvoiceLineAllocation.create(warehouseId,
                BigDecimal.ONE);
        InvoiceLineAllocation second = InvoiceLineAllocation.create(warehouseId,
                BigDecimal.ONE);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        BigDecimal allocated = BigDecimal.valueOf(5);

        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .reconstitute(id, warehouseId, allocated);

        assertEquals(id, allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(allocated, allocation.getAllocated());
    }

    @Test
    void should_PreserveId_When_Reconstituted() {
        UUID specificId = UUID.randomUUID();

        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .reconstitute(specificId, UUID.randomUUID(), BigDecimal.ONE);

        assertEquals(specificId, allocation.getId());
    }

    @Test
    void should_AllowZeroAllocated() {
        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .create(UUID.randomUUID(), BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, allocation.getAllocated());
    }

    @Test
    void should_AllowNullId_When_Reconstituted() {

        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .reconstitute(null, UUID.randomUUID(), BigDecimal.ONE);

        assertNull(allocation.getId());
    }

    @Test
    void should_AllowNullWarehouseId() {
        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(null,
                BigDecimal.ONE);

        assertNull(allocation.getWarehouseId());
    }
}
