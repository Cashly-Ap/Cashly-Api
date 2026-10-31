package com.cashly.cashly_api.inventory.warehouses.stock.application.deduction.services;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockDeductionApi;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public class StockDeductionService implements StockDeductionApi {
    private final WarehouseStockRepository stocks;

    public StockDeductionService(WarehouseStockRepository stocks) {
        this.stocks = stocks;
    }

    @Override
    @Transactional
    public void deduct(List<Allocation> allocations) {
        for (Allocation allocation : allocations) {
            var stock = stocks
                    .findByWarehouseIdAndProductIdForUpdate(
                            allocation.warehouseId(), allocation.productId())
                    .orElseThrow(() -> new IllegalStateException(
                            "No stock found for product "
                                    + allocation.productId() + " in warehouse "
                                    + allocation.warehouseId()));
            stock.removeStock(allocation.quantity());
            stocks.save(stock);
        }
    }
}
