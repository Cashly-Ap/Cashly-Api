package com.cashly.cashly_api.warehouses.domain.entities;

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
                5, 100, 95, null, "BATCH-001", null, null, null, "Stock exit",
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
                20, 50, 70, originId, "BATCH-002", null, null, null, "Adjustment notes",
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

    @Test
    @DisplayName("createWithReference() sets reference fields correctly")
    void shouldCreateWithReferenceFields() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        StockMovement movement = StockMovement.createWithReference(
                warehouseId, productId, MovementType.RETURN_ENTRY,
                10, 0, 10, null, null,
                ReferenceType.RETURN, referenceId, "RET-2025-001",
                "Return movement"
        );

        assertNull(movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.RETURN_ENTRY, movement.getMovementType());
        assertEquals(10, movement.getQuantity());
        assertEquals(0, movement.getPreviousStock());
        assertEquals(10, movement.getNewStock());
        assertEquals(ReferenceType.RETURN, movement.getReferenceType());
        assertEquals(referenceId, movement.getReferenceId());
        assertEquals("RET-2025-001", movement.getReferenceNumber());
        assertEquals("Return movement", movement.getNotes());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    @DisplayName("createWithReference() with SALE_EXIT type")
    void shouldCreateSaleExitWithReference() {
        UUID referenceId = UUID.randomUUID();

        StockMovement movement = StockMovement.createWithReference(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.SALE_EXIT,
                5, 100, 95, null, null,
                ReferenceType.INVOICE, referenceId, "INV-2025-042",
                "Invoice confirmed"
        );

        assertEquals(MovementType.SALE_EXIT, movement.getMovementType());
        assertEquals(ReferenceType.INVOICE, movement.getReferenceType());
        assertEquals(referenceId, movement.getReferenceId());
        assertEquals("INV-2025-042", movement.getReferenceNumber());
    }

    @Test
    @DisplayName("createWithReference() with CANCEL_ENTRY type")
    void shouldCreateCancelEntryWithReference() {
        UUID referenceId = UUID.randomUUID();

        StockMovement movement = StockMovement.createWithReference(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.CANCEL_ENTRY,
                20, 80, 100, null, null,
                ReferenceType.ORDER, referenceId, "ORD-2025-010",
                "Order cancelled"
        );

        assertEquals(MovementType.CANCEL_ENTRY, movement.getMovementType());
        assertEquals(ReferenceType.ORDER, movement.getReferenceType());
        assertEquals(referenceId, movement.getReferenceId());
        assertEquals("ORD-2025-010", movement.getReferenceNumber());
    }

    @Test
    @DisplayName("create() sets reference fields to null for backward compatibility")
    void shouldCreateWithNullReferenceFields() {
        StockMovement movement = StockMovement.create(
                UUID.randomUUID(), UUID.randomUUID(), MovementType.ENTRY,
                50, 0, 50, null, null, "Manual entry"
        );

        assertNull(movement.getReferenceType());
        assertNull(movement.getReferenceId());
        assertNull(movement.getReferenceNumber());
    }

    @Test
    @DisplayName("all movement types are valid")
    void shouldSupportAllMovementTypes() {
        for (MovementType type : MovementType.values()) {
            StockMovement movement = StockMovement.create(
                    UUID.randomUUID(), UUID.randomUUID(), type,
                    1, 0, 1, null, null, "Test"
            );
            assertEquals(type, movement.getMovementType());
        }
    }
}
