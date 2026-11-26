package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceStockCommandTest {
    private final UUID id = UUID.randomUUID();
    @Test
    void should_keep_an_immutable_allocation_snapshot() {
        var allocation = new InvoiceStockCommand.Allocation(id, id, BigDecimal.ONE);
        var source = new ArrayList<>(List.of(allocation));
        var command = new InvoiceStockCommand(id, "INV-1", source);
        source.clear();
        assertEquals(List.of(allocation), command.allocations());
        assertThrows(UnsupportedOperationException.class,
                () -> command.allocations().clear());
    }
    @Test
    void should_reject_missing_invoice_identity() {
        assertThrows(NullPointerException.class,
                () -> new InvoiceStockCommand(null, "INV-1", List.of()));
        assertThrows(NullPointerException.class,
                () -> new InvoiceStockCommand(id, null, List.of()));
    }
    @Test
    void should_reject_missing_allocation_identity() {
        assertThrows(NullPointerException.class,
                () -> new InvoiceStockCommand.Allocation(null, id, BigDecimal.ONE));
        assertThrows(NullPointerException.class,
                () -> new InvoiceStockCommand.Allocation(id, null, BigDecimal.ONE));
    }
    @Test
    void should_reject_nonpositive_quantities() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvoiceStockCommand.Allocation(id, id, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new InvoiceStockCommand.Allocation(id, id, new BigDecimal("-1")));
    }
}
