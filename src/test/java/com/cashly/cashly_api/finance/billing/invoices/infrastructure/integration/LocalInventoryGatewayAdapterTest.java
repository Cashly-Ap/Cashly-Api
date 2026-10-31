package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.InvoiceStockCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalInventoryGatewayAdapterTest {
    private final InvoiceInventoryApi api = mock(InvoiceInventoryApi.class);
    private final LocalInventoryGatewayAdapter adapter = new LocalInventoryGatewayAdapter(
            api);
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final InventoryGateway.InvoiceInventoryCommand command = new InventoryGateway.InvoiceInventoryCommand(
            invoiceId, "INV-2026-00001",
            List.of(new InventoryGateway.Allocation(warehouseId, productId,
                    7)));
    @Test
    void should_translate_invoice_allocations_to_public_inventory_contract() {
        adapter.confirmInvoice(command);
        ArgumentCaptor<InvoiceStockCommand> captor = ArgumentCaptor
                .forClass(InvoiceStockCommand.class);
        verify(api).deductReservations(captor.capture());
        assertEquals(invoiceId, captor.getValue().invoiceId());
        assertEquals("INV-2026-00001", captor.getValue().invoiceNumber());
        assertEquals(List.of(
                new InvoiceStockCommand.Allocation(warehouseId, productId, 7)),
                captor.getValue().allocations());
        verifyNoMoreInteractions(api);
    }
    @Test
    void should_restore_stock_when_cancelled_invoice_was_confirmed() {
        adapter.cancelInvoice(command, true);
        verify(api).restoreStock(any(InvoiceStockCommand.class));
        verifyNoMoreInteractions(api);
    }
    @Test
    void should_release_reservations_when_cancelled_invoice_was_draft() {
        adapter.cancelInvoice(command, false);
        verify(api).releaseReservations(any(InvoiceStockCommand.class));
        verifyNoMoreInteractions(api);
    }
    @Test
    void should_propagate_inventory_failure() {
        doThrow(new IllegalStateException("Insufficient reserved stock"))
                .when(api).deductReservations(any());
        assertThrows(IllegalStateException.class,
                () -> adapter.confirmInvoice(command));
    }
    @Test
    void should_reserve_allocations_through_public_inventory_contract() {
        adapter.reserveAllocations(command.allocations());
        verify(api).reserveAllocations(List.of(
                new InvoiceStockCommand.Allocation(warehouseId, productId, 7)));
        verifyNoMoreInteractions(api);
    }
    @Test
    void should_release_invoice_reservations_through_public_inventory_contract() {
        adapter.releaseInvoiceReservations(command);
        verify(api).releaseReservations(
                new InvoiceStockCommand(invoiceId, "INV-2026-00001",
                        List.of(new InvoiceStockCommand.Allocation(warehouseId,
                                productId, 7))));
        verifyNoMoreInteractions(api);
    }
    @Test
    void should_preserve_invalid_request_response_when_reservation_stock_is_missing() {
        var missing = new com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException(
                productId, warehouseId);
        doThrow(missing).when(api).reserveAllocations(any());
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.reserveAllocations(command.allocations()));
        assertEquals(missing.getMessage(), failure.getMessage());
        assertSame(missing, failure.getCause());
    }
}
