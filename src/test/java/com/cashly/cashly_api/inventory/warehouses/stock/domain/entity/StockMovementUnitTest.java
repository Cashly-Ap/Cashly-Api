package com.cashly.cashly_api.inventory.warehouses.stock.domain.entity;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockMovement Domain Entity Unit Tests")
class StockMovementUnitTest {

    private static java.math.BigDecimal q(int value) {
        return java.math.BigDecimal.valueOf(value);
    }

    @Test
    @DisplayName("create() sets createdAt to now, id is null")
    void shouldCreateWithNullIdAndCurrentTimestamp() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        StockMovement movement = StockMovement.create(warehouseId, productId,
                MovementType.ENTRY, q(10), q(0), q(10), null, null, "Test entry");

        assertNull(movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertEquals(0, q(10).compareTo(movement.getQuantity()));
        assertEquals(0, q(0).compareTo(movement.getPreviousStock()));
        assertEquals(0, q(10).compareTo(movement.getNewStock()));
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

        StockMovement movement = new StockMovement(id, warehouseId, productId,
                MovementType.EXIT, q(5), q(100), q(95), null, "BATCH-001", null, null,
                null, "Stock exit", created);

        assertEquals(id, movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.EXIT, movement.getMovementType());
        assertEquals(0, q(5).compareTo(movement.getQuantity()));
        assertEquals(0, q(100).compareTo(movement.getPreviousStock()));
        assertEquals(0, q(95).compareTo(movement.getNewStock()));
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

        StockMovement movement = new StockMovement(id, warehouseId, productId,
                MovementType.ADJUSTMENT, q(20), q(50), q(70), originId, "BATCH-002",
                null, null, null, "Adjustment notes", created);

        assertEquals(id, movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.ADJUSTMENT, movement.getMovementType());
        assertEquals(0, q(20).compareTo(movement.getQuantity()));
        assertEquals(0, q(50).compareTo(movement.getPreviousStock()));
        assertEquals(0, q(70).compareTo(movement.getNewStock()));
        assertEquals(originId, movement.getOriginMovementId());
        assertEquals("BATCH-002", movement.getTransferBatchId());
        assertEquals("Adjustment notes", movement.getNotes());
        assertEquals(created, movement.getCreatedAt());
    }

    @Test
    @DisplayName("factory method with ENTRY type")
    void shouldCreateEntryMovement() {
        StockMovement movement = StockMovement.create(UUID.randomUUID(),
                UUID.randomUUID(), MovementType.ENTRY, q(50), q(0), q(50), null, null,
                null);

        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertNull(movement.getId());
    }

    @Test
    @DisplayName("factory method with EXIT type")
    void shouldCreateExitMovement() {
        StockMovement movement = StockMovement.create(UUID.randomUUID(),
                UUID.randomUUID(), MovementType.EXIT, q(10), q(100), q(90), null, null,
                null);

        assertEquals(MovementType.EXIT, movement.getMovementType());
    }

    @Test
    @DisplayName("factory method with ADJUSTMENT type")
    void shouldCreateAdjustmentMovement() {
        StockMovement movement = StockMovement.create(UUID.randomUUID(),
                UUID.randomUUID(), MovementType.ADJUSTMENT, q(5), q(50), q(55), null,
                null, "Inventory count");

        assertEquals(MovementType.ADJUSTMENT, movement.getMovementType());
        assertEquals("Inventory count", movement.getNotes());
    }

    @Test
    @DisplayName("createWithReference() sets reference fields correctly")
    void shouldCreateWithReferenceFields() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        StockMovement movement = StockMovement.createWithReference(warehouseId,
                productId, MovementType.RETURN_ENTRY, q(10), q(0), q(10), null, null,
                ReferenceType.RETURN, referenceId, "RET-2025-001",
                "Return movement");

        assertNull(movement.getId());
        assertEquals(warehouseId, movement.getWarehouseId());
        assertEquals(productId, movement.getProductId());
        assertEquals(MovementType.RETURN_ENTRY, movement.getMovementType());
        assertEquals(0, q(10).compareTo(movement.getQuantity()));
        assertEquals(0, q(0).compareTo(movement.getPreviousStock()));
        assertEquals(0, q(10).compareTo(movement.getNewStock()));
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
                UUID.randomUUID(), UUID.randomUUID(), MovementType.SALE_EXIT, q(5),
                q(100), q(95), null, null, ReferenceType.INVOICE, referenceId,
                "INV-2025-042", "Invoice confirmed");

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
                q(20), q(80), q(100), null, null, ReferenceType.ORDER, referenceId,
                "ORD-2025-010", "Order cancelled");

        assertEquals(MovementType.CANCEL_ENTRY, movement.getMovementType());
        assertEquals(ReferenceType.ORDER, movement.getReferenceType());
        assertEquals(referenceId, movement.getReferenceId());
        assertEquals("ORD-2025-010", movement.getReferenceNumber());
    }

    @Test
    @DisplayName("create() sets reference fields to null for backward compatibility")
    void shouldCreateWithNullReferenceFields() {
        StockMovement movement = StockMovement.create(UUID.randomUUID(),
                UUID.randomUUID(), MovementType.ENTRY, q(50), q(0), q(50), null, null,
                "Manual entry");

        assertNull(movement.getReferenceType());
        assertNull(movement.getReferenceId());
        assertNull(movement.getReferenceNumber());
    }

    @Test
    @DisplayName("all movement types are valid")
    void shouldSupportAllMovementTypes() {
        for (MovementType type : MovementType.values()) {
            StockMovement movement = StockMovement.create(UUID.randomUUID(),
                    UUID.randomUUID(), type, q(1), q(0), q(1), null, null, "Test");
            assertEquals(type, movement.getMovementType());
        }
    }
}
