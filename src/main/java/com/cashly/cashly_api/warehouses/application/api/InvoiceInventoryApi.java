package com.cashly.cashly_api.warehouses.application.api;

import java.util.List;

public interface InvoiceInventoryApi {
    void reserveAllocations(List<InvoiceStockCommand.Allocation> allocations);
    void deductReservations(InvoiceStockCommand command);
    void restoreStock(InvoiceStockCommand command);
    void releaseReservations(InvoiceStockCommand command);
}
