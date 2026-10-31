package com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import java.util.List;

public record StockOverviewResponse(PageResponse<StockOverviewItem> stock,
        Summary summary, List<Option> warehouses, List<Option> categories,
        StockOverviewGroupBy groupBy, PageResponse<StockOverviewGroup> groups) {
    public StockOverviewResponse(PageResponse<StockOverviewItem> stock,
            Summary summary, List<Option> warehouses, List<Option> categories) {
        this(stock, summary, warehouses, categories, StockOverviewGroupBy.NONE,
                null);
    }

    public record Summary(long productCount, long warehouseCount,
            long totalQuantity, long reservedQuantity, long availableQuantity) {
    }
    public record Option(String id, String name) {
    }
}
