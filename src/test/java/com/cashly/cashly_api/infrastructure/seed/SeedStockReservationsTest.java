package com.cashly.cashly_api.infrastructure.seed;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeedStockReservationsTest {
    private EntityManager entityManager;
    private WarehouseStockEntity stock;
    private TypedQuery<BigDecimal> orders;
    private TypedQuery<BigDecimal> invoices;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        entityManager = mock(EntityManager.class);
        var stocks = mock(TypedQuery.class);
        orders = mock(TypedQuery.class, RETURNS_SELF);
        invoices = mock(TypedQuery.class, RETURNS_SELF);
        stock = new WarehouseStockEntity();
        stock.setId(UUID.randomUUID());
        stock.setWarehouseId(UUID.randomUUID());
        stock.setProductId(UUID.randomUUID());
        stock.setQuantity(100);
        stock.setReserved(1);
        when(entityManager.createQuery(anyString(),
                eq(WarehouseStockEntity.class))).thenReturn(stocks);
        when(stocks.getResultList()).thenReturn(List.of(stock));
        when(entityManager.createQuery(contains("OrderLineAllocationEntity"),
                eq(BigDecimal.class))).thenReturn(orders);
        when(entityManager.createQuery(contains("InvoiceLineAllocationEntity"),
                eq(BigDecimal.class))).thenReturn(invoices);
    }

    @Test
    void combinesAllDraftDocumentReservationsWithoutChangingStock() {
        when(orders.getSingleResult()).thenReturn(new BigDecimal("3.0000"));
        when(invoices.getSingleResult()).thenReturn(new BigDecimal("2.0000"));

        SeedStockReservations.reconcile(entityManager);

        assertThat(stock.getReserved()).isEqualTo(5);
        assertThat(stock.getQuantity()).isEqualTo(100);
        verify(orders).setParameter("status", "DRAFT");
        verify(invoices).setParameter("status", "DRAFT");
    }

    @Test
    void clearsStaleReservationsWhenNoDraftAllocationsExist() {
        SeedStockReservations.reconcile(entityManager);

        assertThat(stock.getReserved()).isZero();
    }

    @Test
    void refusesReservationsAboveAvailableStock() {
        when(orders.getSingleResult()).thenReturn(new BigDecimal("101"));

        assertThatIllegalStateException().isThrownBy(
                () -> SeedStockReservations.reconcile(entityManager));
        assertThat(stock.getReserved()).isEqualTo(1);
    }

    @Test
    void refusesFractionalReservationsInsteadOfTruncating() {
        when(orders.getSingleResult()).thenReturn(new BigDecimal("1.5"));

        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(
                () -> SeedStockReservations.reconcile(entityManager));
        assertThat(stock.getReserved()).isEqualTo(1);
    }

    @Test
    void repeatedReconciliationKeepsTheSameReservation() {
        when(orders.getSingleResult()).thenReturn(BigDecimal.TEN);

        SeedStockReservations.reconcile(entityManager);
        SeedStockReservations.reconcile(entityManager);

        assertThat(stock.getReserved()).isEqualTo(10);
    }
}
