package com.cashly.cashly_api.purchases.domain.entities;

import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseLineAllocationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseLineAllocation Domain Entity Unit Tests")
class PurchaseLineAllocationUnitTest {

    @Test
    void should_CreateAllocation_When_ValidDataProvided() {
        UUID warehouseId = UUID.randomUUID();
        BigDecimal quantity = BigDecimal.valueOf(10);

        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(warehouseId, quantity);

        assertNotNull(allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(quantity, allocation.getQuantity());
    }

    @Test
    void should_GenerateUniqueId_When_Created() {
        UUID warehouseId = UUID.randomUUID();

        PurchaseLineAllocation first = PurchaseLineAllocation.create(warehouseId, BigDecimal.ONE);
        PurchaseLineAllocation second = PurchaseLineAllocation.create(warehouseId, BigDecimal.ONE);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        PurchaseLineAllocationId id = PurchaseLineAllocationId.generate();
        UUID warehouseId = UUID.randomUUID();
        BigDecimal quantity = BigDecimal.valueOf(5);

        PurchaseLineAllocation allocation = PurchaseLineAllocation.reconstitute(id, warehouseId, quantity);

        assertEquals(id, allocation.getId());
        assertEquals(warehouseId, allocation.getWarehouseId());
        assertEquals(quantity, allocation.getQuantity());
    }

    @Test
    void should_PreserveId_When_Reconstituted() {
        PurchaseLineAllocationId specificId = PurchaseLineAllocationId.of(UUID.randomUUID());

        PurchaseLineAllocation allocation = PurchaseLineAllocation.reconstitute(
                specificId, UUID.randomUUID(), BigDecimal.ONE);

        assertEquals(specificId, allocation.getId());
    }

    @Test
    void should_ThrowException_When_NullIdProvided() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLineAllocation.reconstitute(null, UUID.randomUUID(), BigDecimal.ONE));
    }

    @Test
    void should_ThrowException_When_NullWarehouseIdProvided() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLineAllocation.create(null, BigDecimal.ONE));
    }

    @Test
    void should_ThrowException_When_NullQuantityProvided() {
        assertThrows(NullPointerException.class, () ->
                PurchaseLineAllocation.create(UUID.randomUUID(), null));
    }

    @Test
    void should_ThrowException_When_QuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                PurchaseLineAllocation.create(UUID.randomUUID(), BigDecimal.ZERO));
    }

    @Test
    void should_ThrowException_When_QuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                PurchaseLineAllocation.create(UUID.randomUUID(), new BigDecimal("-5")));
    }
}
