package com.cashly.cashly_api.warehouses.stock.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehouseStock Domain Entity Unit Tests")
class WarehouseStockTest {

    private static final UUID WAREHOUSE_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private WarehouseStock stock;

    @BeforeEach
    void setUp() {
        stock = WarehouseStock.create(WAREHOUSE_ID, PRODUCT_ID);
        stock.addStock(100);
    }

    @Test
    void should_ReserveStock_When_SufficientQuantity() {
        stock.reserve(30);

        assertEquals(100, stock.getQuantity());
        assertEquals(30, stock.getReserved());
        assertEquals(70, stock.getAvailableQuantity());
    }

    @Test
    void should_ThrowException_When_ReservingMoreThanAvailable() {
        stock.reserve(60);
        assertEquals(40, stock.getAvailableQuantity());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> stock.reserve(50));

        assertTrue(exception.getMessage().contains("Insufficient available stock"));
        assertEquals(60, stock.getReserved());
    }

    @Test
    void should_ThrowException_When_ReservingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(0));
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(-5));
    }

    @Test
    void should_ReleaseReserved_When_CancellingReservation() {
        stock.reserve(40);
        assertEquals(40, stock.getReserved());

        stock.releaseReserved(40);

        assertEquals(0, stock.getReserved());
        assertEquals(100, stock.getAvailableQuantity());
    }

    @Test
    void should_ThrowException_When_ReleasingMoreThanReserved() {
        stock.reserve(20);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> stock.releaseReserved(30));

        assertTrue(exception.getMessage().contains("Cannot release more than reserved"));
        assertEquals(20, stock.getReserved());
    }

    @Test
    void should_ThrowException_When_ReleasingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> stock.releaseReserved(0));
        assertThrows(IllegalArgumentException.class, () -> stock.releaseReserved(-1));
    }

    @Test
    void should_DeductReserved_When_ConfirmingOrder() {
        stock.reserve(50);
        assertEquals(50, stock.getReserved());

        stock.deductReserved(50);

        assertEquals(50, stock.getQuantity());
        assertEquals(0, stock.getReserved());
    }

    @Test
    void should_ThrowException_When_DeductingMoreThanReserved() {
        stock.reserve(30);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> stock.deductReserved(40));

        assertTrue(exception.getMessage().contains("Cannot deduct more than reserved"));
    }

    @Test
    void should_ThrowException_When_DeductingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> stock.deductReserved(0));
        assertThrows(IllegalArgumentException.class, () -> stock.deductReserved(-1));
    }

    @Test
    void should_ReturnCorrectAvailableQuantity() {
        assertEquals(100, stock.getAvailableQuantity());

        stock.reserve(30);
        assertEquals(70, stock.getAvailableQuantity());

        stock.reserve(20);
        assertEquals(50, stock.getAvailableQuantity());

        stock.deductReserved(50);
        assertEquals(50, stock.getAvailableQuantity());
    }

    @Test
    void should_UpdateTimestamps_When_ReservingStock() throws InterruptedException {
        WarehouseStock freshStock = WarehouseStock.create(UUID.randomUUID(), UUID.randomUUID());
        freshStock.addStock(50);
        Thread.sleep(5);

        freshStock.reserve(10);

        assertNotNull(freshStock.getUpdatedAt());
        assertTrue(freshStock.getUpdatedAt().isAfter(freshStock.getLastMovementAt()));
    }
}
