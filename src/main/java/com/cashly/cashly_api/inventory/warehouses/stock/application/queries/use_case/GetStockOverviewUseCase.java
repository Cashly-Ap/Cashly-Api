package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockOverviewQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public class GetStockOverviewUseCase {
    private final StockOverviewQueryGateway gateway;

    public GetStockOverviewUseCase(StockOverviewQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public StockOverviewResponse execute(UUID warehouseId, UUID categoryId, String search, int page, int size) {
        return execute(warehouseId, categoryId, search, page, size, StockOverviewGroupBy.NONE);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public StockOverviewResponse execute(UUID warehouseId, UUID categoryId, String search, int page, int size,
                                        StockOverviewGroupBy groupBy) {
        return gateway.search(new StockOverviewFilter(warehouseId, categoryId, search, page, size, groupBy));
    }
}
