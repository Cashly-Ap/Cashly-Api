package com.cashly.cashly_api.warehouses.management.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warehouse Domain Entity Unit Tests")
class WarehouseUnitTest {

    @Test
    @DisplayName("create() creates active warehouse with null id")
    void shouldCreateActiveWarehouseWithNullId() {
        Warehouse warehouse = Warehouse.create(
                "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001"
        );

        assertNull(warehouse.getId());
        assertEquals("WH-001", warehouse.getCode());
        assertEquals("Main Warehouse", warehouse.getName());
        assertEquals("123 Main St", warehouse.getAddress());
        assertEquals("State A", warehouse.getState());
        assertEquals("Municipality B", warehouse.getMunicipality());
        assertEquals("10001", warehouse.getPostalCode());
        assertTrue(warehouse.isActive());
        assertNotNull(warehouse.getCreatedAt());
        assertNotNull(warehouse.getUpdatedAt());
    }

    @Test
    @DisplayName("reconstitute() restores full state")
    void shouldReconstituteWarehouseWithFullState() {
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 6, 1, 15, 0);

        Warehouse warehouse = Warehouse.reconstitute(
                id, "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001",
                false, created, updated
        );

        assertEquals(id, warehouse.getId());
        assertEquals("WH-001", warehouse.getCode());
        assertEquals("Main Warehouse", warehouse.getName());
        assertFalse(warehouse.isActive());
        assertEquals(created, warehouse.getCreatedAt());
        assertEquals(updated, warehouse.getUpdatedAt());
    }

    @Test
    @DisplayName("update() changes all fields and updates updatedAt")
    void shouldUpdateAllFields() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");
        LocalDateTime before = warehouse.getUpdatedAt();

        warehouse.update("New Name", "456 New St", "State C", "Municipality D", "20002", true);

        assertEquals("New Name", warehouse.getName());
        assertEquals("456 New St", warehouse.getAddress());
        assertEquals("State C", warehouse.getState());
        assertEquals("Municipality D", warehouse.getMunicipality());
        assertEquals("20002", warehouse.getPostalCode());
        assertTrue(warehouse.isActive());
        assertTrue(warehouse.getUpdatedAt().isAfter(before) || warehouse.getUpdatedAt().equals(before));
    }

    @Test
    @DisplayName("update() with active=null does not change active flag")
    void shouldNotChangeActiveWhenNull() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");
        assertTrue(warehouse.isActive());

        warehouse.update("New Name", "456 New St", "State C", "Municipality D", "20002", null);

        assertTrue(warehouse.isActive());
    }

    @Test
    @DisplayName("update() validates name not null")
    void shouldThrowWhenNameIsNull() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.update(null, "456 New St", "State C", "Municipality D", "20002", true));
    }

    @Test
    @DisplayName("update() validates name not blank")
    void shouldThrowWhenNameIsBlank() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.update("   ", "456 New St", "State C", "Municipality D", "20002", true));
    }

    @Test
    @DisplayName("update() validates name not over 100 chars")
    void shouldThrowWhenNameOver100Chars() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");
        String longName = "A".repeat(101);

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.update(longName, "456 New St", "State C", "Municipality D", "20002", true));
    }

    @Test
    @DisplayName("activate() sets active to true")
    void shouldActivateWarehouse() {
        Warehouse warehouse = Warehouse.reconstitute(
                UUID.randomUUID(), "WH-001", "Main", "Addr", "State", "Mun", "10001",
                false, LocalDateTime.now(), LocalDateTime.now()
        );
        assertFalse(warehouse.isActive());

        warehouse.activate();

        assertTrue(warehouse.isActive());
        assertNotNull(warehouse.getUpdatedAt());
    }

    @Test
    @DisplayName("deactivate() sets active to false")
    void shouldDeactivateWarehouse() {
        Warehouse warehouse = Warehouse.create("WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");
        assertTrue(warehouse.isActive());

        warehouse.deactivate();

        assertFalse(warehouse.isActive());
        assertNotNull(warehouse.getUpdatedAt());
    }

    @Test
    @DisplayName("code validation: null throws")
    void shouldThrowWhenCodeIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> Warehouse.create(null, "Main Warehouse", "123 Main St",
                        "State A", "Municipality B", "10001"));
    }

    @Test
    @DisplayName("code validation: blank throws")
    void shouldThrowWhenCodeIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> Warehouse.create("   ", "Main Warehouse", "123 Main St",
                        "State A", "Municipality B", "10001"));
    }

    @Test
    @DisplayName("code validation: over 50 chars throws")
    void shouldThrowWhenCodeOver50Chars() {
        String longCode = "A".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> Warehouse.create(longCode, "Main Warehouse", "123 Main St",
                        "State A", "Municipality B", "10001"));
    }

    @Test
    @DisplayName("code is trimmed and uppercased")
    void shouldTrimAndUppercaseCode() {
        Warehouse warehouse = Warehouse.create("  wh-001  ", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001");

        assertEquals("WH-001", warehouse.getCode());
    }

    @Test
    @DisplayName("name is trimmed")
    void shouldTrimName() {
        Warehouse warehouse = Warehouse.create("WH-001", "  Main Warehouse  ", "123 Main St",
                "State A", "Municipality B", "10001");

        assertEquals("Main Warehouse", warehouse.getName());
    }

    @Test
    @DisplayName("equals/hashCode by ID only")
    void shouldBeEqualByIdOnly() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Warehouse w1 = Warehouse.reconstitute(id, "WH-001", "Name 1", "Addr 1",
                "State A", "Mun A", "10001", true, now, now);
        Warehouse w2 = Warehouse.reconstitute(id, "WH-002", "Name 2", "Addr 2",
                "State B", "Mun B", "20002", false, now, now);

        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    @DisplayName("not equal for different IDs")
    void shouldNotBeEqualForDifferentIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Warehouse w1 = Warehouse.reconstitute(id1, "WH-001", "Main", "Addr", "State", "Mun", "10001",
                true, now, now);
        Warehouse w2 = Warehouse.reconstitute(id2, "WH-002", "Main", "Addr", "State", "Mun", "10001",
                true, now, now);

        assertNotEquals(w1, w2);
    }
}
