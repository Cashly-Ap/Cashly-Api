package com.cashly.cashly_api.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.PurchaseStockCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WarehouseInventoryAdapter implements InventoryGateway {
    private final PurchaseInventoryApi inventoryApi;

    public WarehouseInventoryAdapter(PurchaseInventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    @Override
    public void receive(UUID purchaseId, String purchaseNumber, List<Allocation> allocations) {
        inventoryApi.receive(command(purchaseId, purchaseNumber, allocations));
    }

    @Override
    public void reverseReceipt(UUID purchaseId, String purchaseNumber, List<Allocation> allocations) {
        inventoryApi.reverseReceipt(command(purchaseId, purchaseNumber, allocations));
    }

    private PurchaseStockCommand command(UUID purchaseId, String purchaseNumber, List<Allocation> allocations) {
        return new PurchaseStockCommand(purchaseId, purchaseNumber, allocations.stream()
                .map(allocation -> new PurchaseStockCommand.Allocation(allocation.warehouseId(), allocation.productId(), allocation.quantity())).toList());
    }
}
