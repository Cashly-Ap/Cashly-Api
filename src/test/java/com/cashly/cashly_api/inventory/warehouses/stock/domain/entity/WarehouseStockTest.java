package com.cashly.cashly_api.inventory.warehouses.stock.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehouseStock Domain Entity Unit Tests")
class WarehouseStockTest {

    private static final UUID WAREHOUSE_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private WarehouseStock stock;

    private static BigDecimal q(int value) {
        return BigDecimal.valueOf(value);
    }

    @BeforeEach
    void setUp() {
        stock = WarehouseStock.create(WAREHOUSE_ID, PRODUCT_ID);
        stock.addStock(q(100));
    }

    @Test
    void should_ReserveStock_When_SufficientQuantity() {
        stock.reserve(q(30));

        assertEquals(0, q(100).compareTo(stock.getQuantity()));
        assertEquals(0, q(30).compareTo(stock.getReserved()));
        assertEquals(0, q(70).compareTo(stock.getAvailableQuantity()));
    }

    @Test
    void should_ThrowException_When_ReservingMoreThanAvailable() {
        stock.reserve(q(60));
        assertEquals(0, q(40).compareTo(stock.getAvailableQuantity()));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> stock.reserve(q(50)));

        assertTrue(exception.getMessage()
                .contains("Insufficient available stock"));
        assertEquals(0, q(60).compareTo(stock.getReserved()));
    }

    @Test
    void should_ThrowException_When_ReservingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(q(0)));
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(q(-5)));
    }

    @Test
    void should_ReleaseReserved_When_CancellingReservation() {
        stock.reserve(q(40));
        assertEquals(0, q(40).compareTo(stock.getReserved()));

        stock.releaseReserved(q(40));

        assertEquals(0, q(0).compareTo(stock.getReserved()));
        assertEquals(0, q(100).compareTo(stock.getAvailableQuantity()));
    }

    @Test
    void should_ThrowException_When_ReleasingMoreThanReserved() {
        stock.reserve(q(20));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> stock.releaseReserved(q(30)));

        assertTrue(exception.getMessage()
                .contains("Cannot release more than reserved"));
        assertEquals(0, q(20).compareTo(stock.getReserved()));
    }

    @Test
    void should_ThrowException_When_ReleasingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.releaseReserved(q(0)));
        assertThrows(IllegalArgumentException.class,
                () -> stock.releaseReserved(q(-1)));
    }

    @Test
    void should_DeductReserved_When_ConfirmingOrder() {
        stock.reserve(q(50));
        assertEquals(0, q(50).compareTo(stock.getReserved()));

        stock.deductReserved(q(50));

        assertEquals(0, q(50).compareTo(stock.getQuantity()));
        assertEquals(0, q(0).compareTo(stock.getReserved()));
    }

    @Test
    void should_ThrowException_When_DeductingMoreThanReserved() {
        stock.reserve(q(30));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> stock.deductReserved(q(40)));

        assertTrue(exception.getMessage()
                .contains("Cannot deduct more than reserved"));
    }

    @Test
    void should_ThrowException_When_DeductingZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.deductReserved(q(0)));
        assertThrows(IllegalArgumentException.class,
                () -> stock.deductReserved(q(-1)));
    }

    @Test
    void should_ReturnCorrectAvailableQuantity() {
        assertEquals(0, q(100).compareTo(stock.getAvailableQuantity()));

        stock.reserve(q(30));
        assertEquals(0, q(70).compareTo(stock.getAvailableQuantity()));

        stock.reserve(q(20));
        assertEquals(0, q(50).compareTo(stock.getAvailableQuantity()));

        stock.deductReserved(q(50));
        assertEquals(0, q(50).compareTo(stock.getAvailableQuantity()));
    }

    @Test
    void should_UpdateTimestamps_When_ReservingStock()
            throws InterruptedException {
        WarehouseStock freshStock = WarehouseStock.create(UUID.randomUUID(),
                UUID.randomUUID());
        freshStock.addStock(q(50));
        Thread.sleep(5);

        freshStock.reserve(q(10));

        assertNotNull(freshStock.getUpdatedAt());
        assertTrue(freshStock.getUpdatedAt()
                .isAfter(freshStock.getLastMovementAt()));
    }
}
