package com.cashly.cashly_api.sales.orders.infrastructure.integration;

import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockDeductionApi;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Component
public class LocalOrderInventoryGateway implements OrderInventoryGateway {
    private final WarehouseStockRepository stocks;
    private final StockMovementRepository movements;
    private final StockDeductionApi stockDeductionApi;

    @Autowired
    public LocalOrderInventoryGateway(WarehouseStockRepository stocks, StockMovementRepository movements) {
        this.stocks = stocks;
        this.movements = movements;
        this.stockDeductionApi = null;
    }

    public LocalOrderInventoryGateway(StockDeductionApi stockDeductionApi) {
        this.stocks = null;
        this.movements = null;
        this.stockDeductionApi = stockDeductionApi;
    }

    @Override
    @Transactional
    public void deduct(List<Allocation> allocations) {
        if (stockDeductionApi != null) {
            stockDeductionApi.deduct(allocations.stream().map(allocation -> new StockDeductionApi.Allocation(
                    allocation.warehouseId(), allocation.productId(), allocation.quantity())).toList());
            return;
        }
        for (Allocation allocation : allocations) {
            WarehouseStock stock = lockedStock(allocation);
            int previous = stock.getQuantity();
            stock.removeStock(allocation.quantity());
            stocks.save(stock);
            recordMovement(allocation, stock, previous, MovementType.EXIT, null, "Stock deducted for order");
        }
    }

    @Override
    @Transactional
    public void reserve(List<Allocation> allocations) {
        allocations.forEach(allocation -> {
            WarehouseStock stock = lockedStock(allocation);
            stock.reserve(allocation.quantity());
            stocks.save(stock);
        });
    }

    @Override
    @Transactional
    public void release(List<Allocation> allocations) {
        allocations.forEach(allocation -> {
            stocks.findByWarehouseIdAndProductIdForUpdate(allocation.warehouseId(), allocation.productId())
                    .ifPresent(stock -> {
                        stock.releaseReserved(allocation.quantity());
                        stocks.save(stock);
                    });
        });
    }

    @Override
    @Transactional
    public void deductReservations(List<Allocation> allocations, Reference reference) {
        for (Allocation allocation : allocations) {
            WarehouseStock stock = lockedStock(allocation);
            int previous = stock.getQuantity();
            stock.deductReserved(allocation.quantity());
            stocks.save(stock);
            recordMovement(allocation, stock, previous, MovementType.SALE_EXIT, reference, "Order confirmed");
        }
    }

    @Override
    @Transactional
    public void restore(List<Allocation> allocations, Reference reference) {
        for (Allocation allocation : allocations) {
            WarehouseStock stock = lockedStock(allocation);
            int previous = stock.getQuantity();
            stock.addStock(allocation.quantity());
            stocks.save(stock);
            recordMovement(allocation, stock, previous, MovementType.CANCEL_ENTRY, reference, "Order cancelled");
        }
    }

    private WarehouseStock lockedStock(Allocation allocation) {
        return stocks.findByWarehouseIdAndProductIdForUpdate(allocation.warehouseId(), allocation.productId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product " + allocation.productId()
                        + " in warehouse " + allocation.warehouseId()));
    }

    private void recordMovement(Allocation allocation, WarehouseStock stock, int previous,
                                MovementType type, Reference reference, String description) {
        movements.save(StockMovement.createWithReference(allocation.warehouseId(), allocation.productId(), type,
                allocation.quantity(), previous, stock.getQuantity(), null, null,
                reference == null ? null : ReferenceType.ORDER,
                reference == null ? null : reference.id(),
                reference == null ? null : reference.number(),
                reference == null ? description : reference.description()));
    }
}
