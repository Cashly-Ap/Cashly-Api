package com.cashly.cashly_api.warehouses.stock.application.returns;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.ReturnInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.ReturnStockCommand;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

public class ReturnInventoryService implements ReturnInventoryApi {
    private final WarehouseStockRepository stocks;
    private final StockMovementRepository movements;

    public ReturnInventoryService(WarehouseStockRepository stocks, StockMovementRepository movements) {
        this.stocks = stocks;
        this.movements = movements;
    }

    @Override
    @Transactional
    public void restoreStock(ReturnStockCommand command) {
        mutateStock(command, false);
    }

    @Override
    @Transactional
    public void reverseStock(ReturnStockCommand command) {
        mutateStock(command, true);
    }

    private void mutateStock(ReturnStockCommand command, boolean reversing) {
        for (ReturnStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = stocks.findByWarehouseIdAndProductIdForUpdate(allocation.warehouseId(), allocation.productId())
                    .orElseThrow(() -> new IllegalStateException("Stock not found for warehouse=" + allocation.warehouseId()
                            + " product=" + allocation.productId()));
            int previousStock = stock.getQuantity();
            if (reversing) {
                stock.removeStock(allocation.quantity());
            } else {
                stock.addStock(allocation.quantity());
            }
            stocks.save(stock);
            movements.save(StockMovement.createWithReference(allocation.warehouseId(), allocation.productId(),
                    reversing ? MovementType.CANCEL_ENTRY : MovementType.RETURN_ENTRY,
                    allocation.quantity(), previousStock, stock.getQuantity(), null, null, ReferenceType.RETURN,
                    command.returnId(), command.returnNumber(), reversing
                            ? "Return deleted: " + command.returnNumber() + " by " + command.userName()
                            : "Return: " + command.returnNumber()));
        }
    }
}
