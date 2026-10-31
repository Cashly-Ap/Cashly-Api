package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockDeductionApi;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LocalStockDeductionGateway implements StockDeductionGateway {
    private final StockDeductionApi api;

    public LocalStockDeductionGateway(StockDeductionApi api) {
        this.api = api;
    }

    @Override
    public void deduct(List<Allocation> allocations) {
        api.deduct(allocations.stream()
                .map(item -> new StockDeductionApi.Allocation(
                        item.warehouseId(), item.productId(), item.quantity()))
                .toList());
    }
}
