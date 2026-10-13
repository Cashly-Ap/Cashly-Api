package com.cashly.cashly_api.warehouses.application.services;

import com.cashly.cashly_api.warehouses.application.ports.in.PurchaseInventoryApi;
import com.cashly.cashly_api.warehouses.application.ports.in.PurchaseStockCommand;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

public class PurchaseInventoryService implements PurchaseInventoryApi {
    private final WarehouseStockRepository stocks;
    private final StockMovementRepository movements;

    public PurchaseInventoryService(WarehouseStockRepository stocks, StockMovementRepository movements) {
        this.stocks = stocks;
        this.movements = movements;
    }

    @Override
    @Transactional
    public void receive(PurchaseStockCommand command) {
        for (PurchaseStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = stocks.findOrCreateByWarehouseIdAndProductIdForUpdate(
                    allocation.warehouseId(), allocation.productId());
            int previous = stock.getQuantity();
            stock.addStock(allocation.quantity());
            stocks.save(stock);
            record(command, allocation, stock, previous, MovementType.ENTRY, "Compra confirmada");
        }
    }

    @Override
    @Transactional
    public void reverseReceipt(PurchaseStockCommand command) {
        for (PurchaseStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = stocks.findByWarehouseIdAndProductIdForUpdate(allocation.warehouseId(), allocation.productId())
                    .orElseThrow(() -> new IllegalStateException("Product " + allocation.productId()
                            + " has no stock record in warehouse " + allocation.warehouseId()));
            int previous = stock.getQuantity();
            stock.removeStock(allocation.quantity());
            stocks.save(stock);
            record(command, allocation, stock, previous, MovementType.CANCEL_ENTRY, "Compra cancelada");
        }
    }

    private void record(PurchaseStockCommand command, PurchaseStockCommand.Allocation allocation,
                        WarehouseStock stock, int previous, MovementType type, String notes) {
        movements.save(StockMovement.createWithReference(allocation.warehouseId(), allocation.productId(), type,
                allocation.quantity(), previous, stock.getQuantity(), null, null, ReferenceType.PURCHASE,
                command.purchaseId(), command.purchaseNumber(), notes));
    }
}
