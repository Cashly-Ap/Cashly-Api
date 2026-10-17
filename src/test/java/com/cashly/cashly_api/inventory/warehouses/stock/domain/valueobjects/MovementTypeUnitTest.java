package com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MovementType Enum Unit Tests")
class MovementTypeUnitTest {

    @Test
    @DisplayName("ENTRY enum value exists")
    void shouldHaveEntryValue() {
        assertNotNull(MovementType.valueOf("ENTRY"));
        assertEquals(MovementType.ENTRY, MovementType.valueOf("ENTRY"));
    }

    @Test
    @DisplayName("EXIT enum value exists")
    void shouldHaveExitValue() {
        assertNotNull(MovementType.valueOf("EXIT"));
        assertEquals(MovementType.EXIT, MovementType.valueOf("EXIT"));
    }

    @Test
    @DisplayName("ADJUSTMENT enum value exists")
    void shouldHaveAdjustmentValue() {
        assertNotNull(MovementType.valueOf("ADJUSTMENT"));
        assertEquals(MovementType.ADJUSTMENT, MovementType.valueOf("ADJUSTMENT"));
    }

    @Test
    @DisplayName("TRANSFER_IN enum value exists")
    void shouldHaveTransferInValue() {
        assertNotNull(MovementType.valueOf("TRANSFER_IN"));
        assertEquals(MovementType.TRANSFER_IN, MovementType.valueOf("TRANSFER_IN"));
    }

    @Test
    @DisplayName("TRANSFER_OUT enum value exists")
    void shouldHaveTransferOutValue() {
        assertNotNull(MovementType.valueOf("TRANSFER_OUT"));
        assertEquals(MovementType.TRANSFER_OUT, MovementType.valueOf("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("COMPONENT_EXIT enum value exists")
    void shouldHaveComponentExitValue() {
        assertNotNull(MovementType.valueOf("COMPONENT_EXIT"));
        assertEquals(MovementType.COMPONENT_EXIT, MovementType.valueOf("COMPONENT_EXIT"));
    }

    @Test
    @DisplayName("RETURN_ENTRY enum value exists")
    void shouldHaveReturnEntryValue() {
        assertNotNull(MovementType.valueOf("RETURN_ENTRY"));
        assertEquals(MovementType.RETURN_ENTRY, MovementType.valueOf("RETURN_ENTRY"));
    }

    @Test
    @DisplayName("CANCEL_ENTRY enum value exists")
    void shouldHaveCancelEntryValue() {
        assertNotNull(MovementType.valueOf("CANCEL_ENTRY"));
        assertEquals(MovementType.CANCEL_ENTRY, MovementType.valueOf("CANCEL_ENTRY"));
    }

    @Test
    @DisplayName("SALE_EXIT enum value exists")
    void shouldHaveSaleExitValue() {
        assertNotNull(MovementType.valueOf("SALE_EXIT"));
        assertEquals(MovementType.SALE_EXIT, MovementType.valueOf("SALE_EXIT"));
    }

    @Test
    @DisplayName("values() returns 9 items")
    void shouldHaveNineValues() {
        MovementType[] values = MovementType.values();
        assertEquals(9, values.length);
    }
}
