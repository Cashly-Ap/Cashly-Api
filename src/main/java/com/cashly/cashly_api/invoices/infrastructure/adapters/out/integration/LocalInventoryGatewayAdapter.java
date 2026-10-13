package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.warehouses.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.warehouses.application.ports.in.InvoiceStockCommand;
import com.cashly.cashly_api.warehouses.application.exceptions.ProductNotInWarehouseException;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LocalInventoryGatewayAdapter implements InventoryGateway {
    private final InvoiceInventoryApi inventoryApi;

    public LocalInventoryGatewayAdapter(InvoiceInventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    @Override
    public void confirmInvoice(InvoiceInventoryCommand command) {
        inventoryApi.deductReservations(toWarehouseCommand(command));
    }

    @Override
    public void cancelInvoice(InvoiceInventoryCommand command, boolean previouslyConfirmed) {
        InvoiceStockCommand warehouseCommand = toWarehouseCommand(command);
        if (previouslyConfirmed) inventoryApi.restoreStock(warehouseCommand);
        else inventoryApi.releaseReservations(warehouseCommand);
    }

    @Override
    public void reserveAllocations(List<Allocation> allocations) {
        try {
            inventoryApi.reserveAllocations(toWarehouseAllocations(allocations));
        } catch (ProductNotInWarehouseException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    @Override
    public void releaseInvoiceReservations(InvoiceInventoryCommand command) {
        inventoryApi.releaseReservations(toWarehouseCommand(command));
    }

    private InvoiceStockCommand toWarehouseCommand(InvoiceInventoryCommand command) {
        return new InvoiceStockCommand(command.invoiceId(), command.invoiceNumber(),
                toWarehouseAllocations(command.allocations()));
    }

    private List<InvoiceStockCommand.Allocation> toWarehouseAllocations(List<Allocation> allocations) {
        return allocations.stream()
                .map(a -> new InvoiceStockCommand.Allocation(a.warehouseId(), a.productId(), a.quantity()))
                .toList();
    }
}
