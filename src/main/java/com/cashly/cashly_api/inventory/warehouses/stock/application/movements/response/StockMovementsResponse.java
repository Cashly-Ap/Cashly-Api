package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.math.BigDecimal;
import java.util.List;

public record StockMovementsResponse(Summary summary,
        List<WarehouseOption> warehouses, List<ProductOption> products,
        StockMovementGroupBy groupBy,
        PageResponse<StockMovementResponse> movements,
        PageResponse<StockMovementGroup> groups) {

    public StockMovementsResponse {
        warehouses = warehouses == null ? List.of() : List.copyOf(warehouses);
        products = products == null ? List.of() : List.copyOf(products);
    }

    public static StockMovementsResponse ungrouped(Summary summary,
            List<WarehouseOption> warehouses, List<ProductOption> products,
            PageResponse<StockMovementResponse> movements) {
        return new StockMovementsResponse(summary, warehouses, products,
                StockMovementGroupBy.NONE, movements, null);
    }

    public static StockMovementsResponse grouped(Summary summary,
            List<WarehouseOption> warehouses, List<ProductOption> products,
            StockMovementGroupBy groupBy,
            PageResponse<StockMovementGroup> groups) {
        return new StockMovementsResponse(summary, warehouses, products, groupBy,
                null, groups);
    }

    public record Summary(long movementCount, long productCount,
            long warehouseCount, BigDecimal entryQuantity,
            BigDecimal exitQuantity, BigDecimal netQuantity) {
        public static Summary empty() {
            return new Summary(0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }
    }

    public record WarehouseOption(String id, String name) {
    }

    public record ProductOption(String id, String name, String code) {
    }
}
