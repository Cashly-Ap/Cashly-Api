package com.cashly.cashly_api.invoices.application.ports.out;

import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import java.util.List;
import java.util.UUID;

public interface InventoryGateway {
    void reserveAllocations(List<Allocation> allocations);
    void releaseInvoiceReservations(InvoiceInventoryCommand command);
    void confirmInvoice(InvoiceInventoryCommand command);
    void cancelInvoice(InvoiceInventoryCommand command, boolean previouslyConfirmed);

    record InvoiceInventoryCommand(UUID invoiceId, String invoiceNumber, List<Allocation> allocations) {
        public InvoiceInventoryCommand {
            allocations = List.copyOf(allocations);
        }
        public static InvoiceInventoryCommand from(Invoice invoice) {
            return new InvoiceInventoryCommand(invoice.getId().value(), invoice.getInvoiceNumber().value(),
                    invoice.getLines().stream().flatMap(line -> line.getAllocations().stream()
                            .map(a -> new Allocation(a.getWarehouseId(), line.getProductId(),
                                    a.getAllocated().intValue()))).toList());
        }
    }
    record Allocation(UUID warehouseId, UUID productId, int quantity) {}
}
