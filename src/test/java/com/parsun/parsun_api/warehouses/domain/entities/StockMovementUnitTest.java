package com.parsun.parsun_api.warehouses.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockMovement Domain Entity Unit Tests")
class StockMovementUnitTest {

    @Test
    @DisplayName("create() sets createdAt to now, id is null")
    void shouldCreateWithNullIdAndCurrentTimestamp() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        StockMovement movement = StockMovement.create(
                warehouseId, productId, MovementType.ENTRY,
                10, 0, 10, null, null, "Test entry"
        );

        assertNull(movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertEquals(10, movement.getQuantity());
        assertEquals(0, movement.getPreviousStock());
        assertEquals(10, movement.getNewStock());
        assertNull(movement.getOriginMovementId());
        assertNull(movement.getTransferBatchId());
        assertEquals("Test entry", movement.getNotes());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    @DisplayName("reconstitute() with id restores full state")
    void shouldReconstituteWithFullState() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.of(2024, 3, 15, 10, 30);

        StockMovement movement = new StockMovement(
                id, warehouseId, productId, MovementType.EXIT,
                5, 100, 95, null, "BATCH-001", "Stock exit",
                created
        );

        assertEquals(id, movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.EXIT, movement.getMovementType());
        assertEquals(5, movement.getQuantity());
        assertEquals(100, movement.getPreviousStock());
        assertEquals(95, movement.getNewStock());
        assertEquals("BATCH-001", movement.getTransferBatchId());
        assertEquals("Stock exit", movement.getNotes());
        assertEquals(created, movement.getCreatedAt());
    }

    @Test
    @DisplayName("all getters return correct values")
    void shouldReturnCorrectValuesFromGetters() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID originId = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now();

        StockMovement movement = new StockMovement(
                id, warehouseId, productId, MovementType.ADJUSTMENT,
                20, 50, 70, originId, "BATCH-002", "Adjustment notes",
                created
        );

        assertEquals(id, movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.ADJUSTMENT, movement.getMovementType());
        assertEquals(20, movement.getQuantity());
        assertEquals(50, movement.getPreviousStock());
        assertEquals(70, movement.getNewStock());
        assertEquals(originId, movement.getOriginMovementId());
        assertEquals("BATCH-002", movement.getTransferBatchId());
        assertEquals("Adjustment notes", movement.getNotes());
        assertEquals(created, movement.getCreatedAt());
    }

    @Test
    @DisplayName("factory method with ENTRY type")
    void shouldCreateEntryMovement() {
        StockMovement movement = StockMovement.create(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.ENTRY,
                50, 0, 50, null, null, null
        );

        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertNull(movement.getId());
    }

    @Test
    @DisplayName("factory method with EXIT type")
    void shouldCreateExitMovement() {
        StockMovement movement = StockMovement.create(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.EXIT,
                10, 100, 90, null, null, null
        );

        assertEquals(MovementType.EXIT, movement.getMovementType());
    }

    @Test
    @DisplayName("factory method with ADJUSTMENT type")
    void shouldCreateAdjustmentMovement() {
        StockMovement movement = StockMovement.create(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.ADJUSTMENT,
                5, 50, 55, null, null, "Inventory count"
        );

        assertEquals(MovementType.ADJUSTMENT, movement.getMovementType());
        assertEquals("Inventory count", movement.getNotes());
    }
}
