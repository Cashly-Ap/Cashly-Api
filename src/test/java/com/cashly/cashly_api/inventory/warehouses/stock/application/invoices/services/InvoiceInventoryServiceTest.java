package com.cashly.cashly_api.inventory.warehouses.stock.application.invoices.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.InvoiceStockCommand;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.InvoiceStockCommand.Allocation;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class InvoiceInventoryServiceTest {
    private final WarehouseStockRepository stocks = mock(
            WarehouseStockRepository.class);
    private final StockMovementRepository movements = mock(
            StockMovementRepository.class);
    private final UUID warehouse = UUID.randomUUID();
    private final UUID product = UUID.randomUUID();
    private final UUID invoice = UUID.randomUUID();
    private InvoiceInventoryService useCase;
    private InvoiceStockCommand command;
    @BeforeEach
    void setUp() {
        useCase = new InvoiceInventoryService(stocks, movements);
        command = new InvoiceStockCommand(invoice, "INV-2026-00001",
                List.of(new Allocation(warehouse, product, 10)));
    }
    @Test
    void should_deduct_reserved_stock_and_preserve_invoice_movement_metadata() {
        WarehouseStock stock = givenStock();
        useCase.deductReservations(command);
        assertEquals(90, stock.getQuantity());
        assertEquals(0, stock.getReserved());
        verify(stocks).save(stock);
        verify(movements)
                .save(argThat(m -> m.getMovementType() == MovementType.SALE_EXIT
                        && m.getQuantity() == 10 && m.getPreviousStock() == 100
                        && m.getNewStock() == 90
                        && m.getReferenceType() == ReferenceType.INVOICE
                        && invoice.equals(m.getReferenceId())
                        && "INV-2026-00001".equals(m.getReferenceNumber())
                        && "Invoice confirmed".equals(m.getNotes())
                        && m.getOriginMovementId() == null
                        && m.getTransferBatchId() == null));
        verify(stocks, never()).findByWarehouseIdAndProductId(any(), any());
    }
    @Test
    void should_restore_confirmed_stock_and_preserve_cancellation_metadata() {
        WarehouseStock stock = givenStock();
        useCase.restoreStock(command);
        assertEquals(110, stock.getQuantity());
        assertEquals(10, stock.getReserved());
        verify(movements).save(
                argThat(m -> m.getMovementType() == MovementType.CANCEL_ENTRY
                        && m.getQuantity() == 10 && m.getPreviousStock() == 100
                        && m.getNewStock() == 110
                        && invoice.equals(m.getReferenceId())
                        && m.getReferenceType() == ReferenceType.INVOICE
                        && "INV-2026-00001".equals(m.getReferenceNumber())
                        && "Invoice cancelled".equals(m.getNotes())));
    }
    @Test
    void should_release_draft_reservations_without_creating_movement() {
        WarehouseStock stock = givenStock();
        useCase.releaseReservations(command);
        assertEquals(100, stock.getQuantity());
        assertEquals(0, stock.getReserved());
        verify(stocks).save(stock);
        verifyNoInteractions(movements);
    }
    @Test
    void should_fail_when_confirmation_stock_is_missing() {
        assertThrows(ProductNotInWarehouseException.class,
                () -> useCase.deductReservations(command));
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }
    @Test
    void should_fail_when_cancellation_stock_is_missing() {
        assertThrows(ProductNotInWarehouseException.class,
                () -> useCase.restoreStock(command));
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }
    @Test
    void should_fail_when_draft_reservation_stock_is_missing() {
        assertThrows(ProductNotInWarehouseException.class,
                () -> useCase.releaseReservations(command));
        verify(stocks, never()).save(any());
    }
    @Test
    void should_reject_confirmation_exceeding_reserved_stock() {
        givenStock();
        InvoiceStockCommand invalid = new InvoiceStockCommand(invoice,
                "INV-2026-00001",
                List.of(new Allocation(warehouse, product, 11)));
        assertThrows(IllegalStateException.class,
                () -> useCase.deductReservations(invalid));
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }
    @Test
    void should_reserve_allocations_using_locked_stock_without_movement() {
        WarehouseStock stock = givenStock();
        useCase.reserveAllocations(command.allocations());
        assertEquals(100, stock.getQuantity());
        assertEquals(20, stock.getReserved());
        verify(stocks).save(stock);
        verifyNoInteractions(movements);
    }
    @Test
    void should_fail_reservation_when_stock_is_missing() {
        assertThrows(ProductNotInWarehouseException.class,
                () -> useCase.reserveAllocations(command.allocations()));
        verify(stocks, never()).save(any());
        verifyNoInteractions(movements);
    }
    @Test
    void should_reject_reservation_exceeding_available_stock() {
        givenStock();
        assertThrows(IllegalStateException.class,
                () -> useCase.reserveAllocations(
                        List.of(new Allocation(warehouse, product, 91))));
        verify(stocks, never()).save(any());
    }
    private WarehouseStock givenStock() {
        WarehouseStock stock = WarehouseStock.create(warehouse, product);
        stock.addStock(100);
        stock.reserve(10);
        when(stocks.findByWarehouseIdAndProductIdForUpdate(warehouse, product))
                .thenReturn(Optional.of(stock));
        return stock;
    }
}
