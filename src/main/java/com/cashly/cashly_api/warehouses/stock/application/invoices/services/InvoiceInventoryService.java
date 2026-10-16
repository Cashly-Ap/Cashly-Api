package com.cashly.cashly_api.warehouses.stock.application.invoices.services;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.InvoiceStockCommand;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class InvoiceInventoryService implements InvoiceInventoryApi {
    private final WarehouseStockRepository stocks;
    private final StockMovementRepository movements;

    public InvoiceInventoryService(WarehouseStockRepository stocks, StockMovementRepository movements) {
        this.stocks = stocks;
        this.movements = movements;
    }

    @Override
    @Transactional
    public void deductReservations(InvoiceStockCommand command) {
        for (InvoiceStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = lockedStock(allocation);
            int previous = stock.getQuantity();
            stock.deductReserved(allocation.quantity());
            stocks.save(stock);
            recordMovement(command, allocation, stock, previous, MovementType.SALE_EXIT, "Invoice confirmed");
        }
    }

    @Override
    @Transactional
    public void restoreStock(InvoiceStockCommand command) {
        for (InvoiceStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = lockedStock(allocation);
            int previous = stock.getQuantity();
            stock.addStock(allocation.quantity());
            stocks.save(stock);
            recordMovement(command, allocation, stock, previous, MovementType.CANCEL_ENTRY, "Invoice cancelled");
        }
    }

    @Override
    @Transactional
    public void releaseReservations(InvoiceStockCommand command) {
        for (InvoiceStockCommand.Allocation allocation : command.allocations()) {
            WarehouseStock stock = lockedStock(allocation);
            stock.releaseReserved(allocation.quantity());
            stocks.save(stock);
        }
    }

    @Override
    @Transactional
    public void reserveAllocations(List<InvoiceStockCommand.Allocation> allocations) {
        for (InvoiceStockCommand.Allocation allocation : allocations) {
            WarehouseStock stock = lockedStock(allocation);
            stock.reserve(allocation.quantity());
            stocks.save(stock);
        }
    }

    private WarehouseStock lockedStock(InvoiceStockCommand.Allocation allocation) {
        return stocks.findByWarehouseIdAndProductIdForUpdate(allocation.warehouseId(), allocation.productId())
                .orElseThrow(() -> new ProductNotInWarehouseException(allocation.productId(), allocation.warehouseId()));
    }

    private void recordMovement(InvoiceStockCommand command, InvoiceStockCommand.Allocation allocation,
                                WarehouseStock stock, int previous, MovementType movementType, String notes) {
        movements.save(StockMovement.createWithReference(allocation.warehouseId(), allocation.productId(),
                movementType, allocation.quantity(), previous, stock.getQuantity(), null, null,
                ReferenceType.INVOICE, command.invoiceId(), command.invoiceNumber(), notes));
    }
}
